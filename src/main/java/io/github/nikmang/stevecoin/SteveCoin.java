/*
 * SteveCoin - a spigot cryptocurrency plugin.
 * Copyright (C) 2018  Nik Gil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.nikmang.stevecoin;

import io.github.nikmang.stevecoin.cmds.Balance;
import io.github.nikmang.stevecoin.cmds.Send;
import io.github.nikmang.stevecoin.crypto.Blockchain;
import io.github.nikmang.stevecoin.crypto.Wallet;
import io.github.nikmang.stevecoin.listeners.OnJoin;
import io.github.nikmang.stevecoin.listeners.SignClick;
import io.github.nikmang.stevecoin.listeners.SignCreation;
import io.github.nikmang.stevecoin.utils.cmds.CommandMaster;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main Spigot class.
 */
public class SteveCoin extends JavaPlugin {

    public Map<UUID, Wallet> wallets;
    public Blockchain blockchain;

    private Economy provider;

    @Override
    public void onEnable() {
        Security.addProvider(new BouncyCastleProvider());

        this.wallets = new HashMap<>();
        this.blockchain = new Blockchain();

        //Make a datafolder
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdirs()) {
                LogManager.getRootLogger().warn("Could not create main plugin folder. Need to do so manually");
            }
        }

        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        boolean debug = this.getConfig().getBoolean("debug");

        if (!debug) {
            Configurator.setRootLevel(Level.INFO);
        }

        //TODO: listeners
        Bukkit.getPluginManager().registerEvents(new SignClick(), this);
        Bukkit.getPluginManager().registerEvents(new OnJoin(this), this);
        Bukkit.getPluginManager().registerEvents(new SignCreation(), this);

        //TODO: commands
        CommandMaster cm = new CommandMaster("Steve Coin");
        this.getCommand("stevecoin").setExecutor(cm);

        cm.addCommand(new Send(this));
        cm.addCommand(new Balance(this));
        //TODO: blockchain loading
    }

    private void hook() {
        this.provider = new EconomyImplementor(this);
        Bukkit.getServicesManager().register(Economy.class, this.provider, this, ServicePriority.Normal);
        LogManager.getRootLogger().info("SteveCoin registered as economy");
    }

    private void unhook() {
        Bukkit.getServicesManager().unregister(Economy.class, this.provider);
        LogManager.getRootLogger().info("SteveCoin unregistered");
    }
}
