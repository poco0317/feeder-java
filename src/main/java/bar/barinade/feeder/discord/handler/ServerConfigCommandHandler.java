package bar.barinade.feeder.discord.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import bar.barinade.feeder.discord.serverconfig.service.SubredditService;
import bar.barinade.feeder.discord.serverconfig.service.ServerConfigService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
@Scope("prototype")
public class ServerConfigCommandHandler extends CommandHandlerBase {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ServerConfigCommandHandler.class);
	
	private static final String BASE_CMD_NAME = "feeder";
	
	private static final String NAME_CMD_SET = "set";
	private static final String NAME_CMD_REMOVE = "remove";
	private static final String NAME_CMD_OUTPUT = "channel";
	
	private static final String NAME_CMD_HUMBLE_TOGGLE = "humbletoggle";
	private static final String NAME_CMD_PARTNER = "humblepartner";
	private static final String NAME_CMD_REMOVE_PARTNER = "humbleunpartner";
	
	private static final String OPTION_CHANNEL = "channel";
	private static final String OPTION_UPVOTES = "upvotes";
	private static final String OPTION_SUBREDDIT = "subreddit";
	private static final String OPTION_CODE = "code";
	private static final String OPTION_ONOFF = "on-off";
	
	@Autowired
	private ServerConfigService configService;
	
	@Autowired
	private SubredditService subs;
	
	@Value("${discord.ownerid}")
	private String ownerId;
	
	@Override
	public CommandData[] getCommandsToUpsert() {
		return new CommandData[] {
				new CommandData(BASE_CMD_NAME, "Configure social media feeds")
				.addSubcommands(
						new SubcommandData(NAME_CMD_SET, "Add or modify a subreddit feed")
							.addOption(OptionType.STRING, OPTION_SUBREDDIT, "Target subreddit", true)
							.addOption(OptionType.INTEGER, OPTION_UPVOTES, "Minimum upvote threshold", true),
						new SubcommandData(NAME_CMD_REMOVE, "Remove a subreddit feed")
							.addOption(OptionType.STRING, OPTION_SUBREDDIT, "Target subreddit", true),
						new SubcommandData(NAME_CMD_OUTPUT, "Set the output channel")
							.addOption(OptionType.CHANNEL, OPTION_CHANNEL, "Target channel", true),
						new SubcommandData(NAME_CMD_HUMBLE_TOGGLE, "Toggle Humble Bundle posting")
							.addOption(OptionType.BOOLEAN, OPTION_ONOFF, "On or Off"),
						new SubcommandData(NAME_CMD_PARTNER, "Set the Humble Bundle partner code")
							.addOption(OptionType.STRING, OPTION_CODE, "URL Query String"),
						new SubcommandData(NAME_CMD_REMOVE_PARTNER, "Unset the Humble Bundle partner code")
							
				)
		};
	}
	
	private boolean hasPermission(SlashCommandEvent event) {
		Member mmbr = event.getMember();
		if (mmbr != null
				&& !mmbr.getId().equals(ownerId)
				&& !mmbr.isOwner()
				&& !mmbr.hasPermission(Permission.ADMINISTRATOR)
				&& !mmbr.hasPermission(Permission.MANAGE_SERVER)) {
			m_logger.info("{} attempted to use config command without having permission", mmbr.getId());
			event.getHook().editOriginal("You must have Manage Server or Administrator permissions to use this command.").queue();;
			return false;
		}
		return true;
	}
	
	void cmd_feeder(SlashCommandEvent event) {
		if (!hasPermission(event))
			return;
		
		final String subcmd = event.getSubcommandName();
		if (subcmd != null) {
			if (subcmd.equals(NAME_CMD_SET)) {
				handleSet(event);
			} else if (subcmd.equals(NAME_CMD_REMOVE)) {
				handleRemove(event);
			} else if (subcmd.equals(NAME_CMD_OUTPUT)) {
				handleOutput(event);
			} else if (subcmd.equals(NAME_CMD_HUMBLE_TOGGLE)) {
				handleBundleSet(event);
			} else if (subcmd.equals(NAME_CMD_PARTNER)) {
				handleBundlePartner(event);
			} else if (subcmd.equals(NAME_CMD_REMOVE_PARTNER)) {
				handleBundlePartnerUnset(event);
			} else {
				// ???
				m_logger.warn("{} attmpted to use unimplemented config command {}", event.getMember().getId(), subcmd);
				event.getHook().editOriginal("That command does not exist. You shouldn't see this").queue();
			}
		} else {
			// ???
			m_logger.warn("{} attempted to use a null config command", event.getMember().getId());
			event.getHook().editOriginal("That command does not exist. You shouldn't see this").queue();
		}
	}
	
	private void handleBundleSet(SlashCommandEvent event) {
		final Long guildId = event.getGuild().getIdLong();
		final Boolean toggle = event.getOption(OPTION_ONOFF).getAsBoolean();
		
		configService.setPostHumbleBundleLinks(guildId, toggle);
		
		if (configService.getPostHumbleBundleLinks(guildId)) {
			event.getHook().editOriginal("Turned on Humble Bundle Posting.").queue();
		} else {
			event.getHook().editOriginal("Turned off Humble Bundle Posting.").queue();
		}
	}
	
	private void handleBundlePartner(SlashCommandEvent event) {
		final Long guildId = event.getGuild().getIdLong();
		final String code = event.getOption(OPTION_CODE).getAsString();
		
		configService.setHumbleBundlePartnerCode(guildId, code);
		
		event.getHook().editOriginal("Set Humble Bundle Partner Code to `"+code+"`. Remember to turn on Humble Bundle Posting.").queue();
	}
	
	private void handleBundlePartnerUnset(SlashCommandEvent event) {
		final Long guildId = event.getGuild().getIdLong();

		configService.setHumbleBundlePartnerCode(guildId, null);
		
		event.getHook().editOriginal("Removed Humble Bundle Partner Code from future posts.").queue();
	}
	
	private void handleSet(SlashCommandEvent event) {
		final String sub = event.getOption(OPTION_SUBREDDIT).getAsString();
		final Long upvotes = event.getOption(OPTION_UPVOTES).getAsLong();
		
		if (sub == null || upvotes == null) {
			m_logger.warn("{} failed to pass all args to Set", event.getMember().getId());
			event.getHook().editOriginal("You are missing arguments.").queue();
			return;
		}
		
		final Long guildId = event.getGuild().getIdLong();
		
		if (subs.set(guildId, sub, upvotes.intValue())) {
			event.getHook().editOriginal("Set subreddit "+sub+" with "+upvotes+" upvotes.").queue();
		} else {
			event.getHook().editOriginal("Updated subreddit "+sub+" to "+upvotes+" upvotes.").queue();
		}
	}
	private void handleRemove(SlashCommandEvent event) {
		final String sub = event.getOption(OPTION_SUBREDDIT).getAsString();
		
		if (sub == null) {
			m_logger.warn("{} failed to pass all args to Remove", event.getMember().getId());
			event.getHook().editOriginal("You are missing the Subreddit argument.").queue();
			return;
		}
		
		final Long guildId = event.getGuild().getIdLong();
		
		if (subs.deleteSub(guildId, sub)) {
			event.getHook().editOriginal("Subreddit "+sub+" was removed from this server.").queue();
		} else {
			event.getHook().editOriginal("Subreddit "+sub+" is not tracked by this server.").queue();
		}
	}
	private void handleOutput(SlashCommandEvent event) {
		final ChannelType chantype = event.getOption(OPTION_CHANNEL).getChannelType();
		if (!chantype.equals(ChannelType.TEXT)) {
			m_logger.warn("{} failed to pass a text channel to Output", event.getMember().getId());
			event.getHook().editOriginal("You must specify a Text Channel. Your channel was of type '"+chantype.toString()+"'").queue();
			return;
		}
		final MessageChannel channel = event.getOption(OPTION_CHANNEL).getAsMessageChannel();
		
		final Long guildId = event.getGuild().getIdLong();
		final Long channelId = channel.getIdLong();
		
		configService.setOutputChannel(guildId, channelId);
		event.getHook().editOriginal("Set output channel to "+channel.getName()).queue();
	}
}
