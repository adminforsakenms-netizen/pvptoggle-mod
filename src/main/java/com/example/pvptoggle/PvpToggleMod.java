package com.example.pvptoggle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PvpToggleMod implements ModInitializer {

	private static final Map<UUID, Boolean> pvpEnabled = new HashMap<>();

	private static boolean isPvpOn(UUID uuid) {
		return pvpEnabled.getOrDefault(uuid, true);
	}

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("pvp")
				.then(CommandManager.literal("on").executes(ctx -> setPvp(ctx, true)))
				.then(CommandManager.literal("off").executes(ctx -> setPvp(ctx, false)))
				.then(CommandManager.literal("status").executes(PvpToggleMod::showStatus))
			);
		});

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof PlayerEntity defender
					&& source.getAttacker() instanceof PlayerEntity attacker
					&& defender != attacker) {

				boolean defenderPvp = isPvpOn(defender.getUuid());
				boolean attackerPvp = isPvpOn(attacker.getUuid());

				if (!defenderPvp || !attackerPvp) {
					String siapaYangMatiin = !defenderPvp
							? defender.getName().getString()
							: attacker.getName().getString();

					attacker.sendMessage(
							Text.literal("§cSerangan gagal: PvP " + siapaYangMatiin + " sedang mati (§7/pvp status§c)."),
							true
					);
					return false;
				}
			}
			return true;
		});
	}

	private static int setPvp(CommandContext<ServerCommandSource> ctx, boolean enabled) {
		ServerCommandSource source = ctx.getSource();
		if (source.getPlayer() == null) {
			source.sendError(Text.literal("Command ini cuma bisa dipakai pemain in-game."));
			return 0;
		}

		PlayerEntity player = source.getPlayer();
		pvpEnabled.put(player.getUuid(), enabled);

		String pesan = enabled
				? "§aPvP kamu sekarang §lMENYALA§r§a. Pemain lain bisa menyerangmu."
				: "§ePvP kamu sekarang §lMATI§r§e. Kamu aman dari serangan pemain lain (dan tidak bisa menyerang mereka).";

		source.sendFeedback(() -> Text.literal(pesan), false);
		return 1;
	}

	private static int showStatus(CommandContext<ServerCommandSource> ctx) {
		ServerCommandSource source = ctx.getSource();
		if (source.getPlayer() == null) {
			source.sendError(Text.literal("Command ini cuma bisa dipakai pemain in-game."));
			return 0;
		}

		boolean status = isPvpOn(source.getPlayer().getUuid());
		source.sendFeedback(
				() -> Text.literal("Status PvP kamu: " + (status ? "§aMENYALA" : "§eMATI")),
				false
		);
		return 1;
	}
}
