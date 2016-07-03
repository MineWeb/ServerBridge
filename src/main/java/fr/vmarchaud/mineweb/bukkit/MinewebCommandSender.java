package fr.vmarchaud.mineweb.bukkit;

import org.bukkit.craftbukkit.v1_8_R1.command.ServerCommandSender;

public class MinewebCommandSender extends ServerCommandSender {

    @Override
    public void sendMessage(String message) {
        //RemoteControlCommandListener.instance.sendMessage(message + "\n"); // Send a newline after each message, to preserve formatting.
    	
    }

    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public String getName() {
        return "Rcon";
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        // unsupported
    	return ;
    }
}
