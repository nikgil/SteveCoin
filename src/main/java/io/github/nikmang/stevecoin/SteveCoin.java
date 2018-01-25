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

import io.github.nikmang.stevecoin.crypto.Blockchain;
import io.github.nikmang.stevecoin.crypto.Wallet;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

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
        this.wallets = new HashMap<>();

        //Make a datafolder
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdirs()) {
                Bukkit.getLogger().warning("Could not create main plugin folder. Need to do so manually");
            }
        }

        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        boolean debug = this.getConfig().getBoolean("debug");

        if (!debug) {
            Configurator.setRootLevel(Level.INFO);
        }
    }

    private void hook() {
        this.provider = new EconomyImplementor(this);
        Bukkit.getServicesManager().register(Economy.class, this.provider, this, ServicePriority.Normal);
        Bukkit.getLogger().info("SteveCoin registered as economy");
    }

    private void unhook() {
        Bukkit.getServicesManager().unregister(Economy.class, this.provider);
        Bukkit.getLogger().info("SteveCoin unregistered");
    }
}
