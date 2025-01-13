package ayohee.create_cct_web;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import javax.naming.ldap.Control;

public class WebInterfaceCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        LiteralArgumentBuilder<CommandSourceStack> restartSubcommand = Commands.literal("restart");
        restartSubcommand.then(Commands.literal("website").executes(WebInterfaceCommand::restartWebsite));
        restartSubcommand.then(Commands.literal("control").executes(WebInterfaceCommand::restartControl));

        LiteralArgumentBuilder<CommandSourceStack> webInterfaceCommand = Commands.literal("webinterface");
        webInterfaceCommand.then(restartSubcommand);

        dispatcher.register(webInterfaceCommand);
    }

    private static int restartControl(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("Restarting control server..."));
        }

        LOGGER.info("Restarting control server...");
        ControlServer.restart();

        return Command.SINGLE_SUCCESS;
    }

    private static int restartWebsite(CommandContext<CommandSourceStack> command) {
        if(command.getSource().getEntity() instanceof Player player) {
            player.sendSystemMessage(Component.literal("Restarting website..."));
        }

        LOGGER.info("Restarting website...");
        WebsiteServer.restart();;

        return Command.SINGLE_SUCCESS;
    }
}
