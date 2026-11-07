package com.example.villagerjobunlock;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Dans le jeu normal, un villageois qui a déjà fait un trade (XP > 0)
 * garde son métier pour toujours, même si son bloc de métier est cassé.
 * Ce mod retire ce blocage : si le bloc de métier a disparu, le villageois
 * redevient sans métier et peut en prendre un nouveau.
 */
@Mod(VillagerJobUnlock.MODID)
@Mod.EventBusSubscriber(modid = VillagerJobUnlock.MODID)
public class VillagerJobUnlock {
    public static final String MODID = "villagerjobunlock";

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!(villager.level instanceof ServerLevel level)) return;

        // on vérifie une fois par seconde, pas à chaque tick
        if (villager.tickCount % 20 != 0) return;

        VillagerData data = villager.getVillagerData();
        VillagerProfession profession = data.getProfession();

        // ignore les villageois sans métier et les idiots
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) return;

        // ne touche pas à un villageois en train de commercer
        if (villager.isTrading()) return;

        // le bloc de métier existe encore : on ne change rien
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE)) return;

        // bloc de métier perdu (tous niveaux, Maître compris) : on retire le blocage
        // XP à 0, niveau 1 (novice) et plus de métier, pour qu'il puisse en reprendre un
        villager.setVillagerXp(0);
        villager.setVillagerData(data.setProfession(VillagerProfession.NONE).setLevel(1));
        villager.refreshBrain(level);
    }
}
