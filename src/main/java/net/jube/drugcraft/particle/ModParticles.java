package net.jube.drugcraft.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.jube.drugcraft.DrugCraft;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final SimpleParticleType MARIJUANA_PLANT_PARTICLE =
            registerParticle("marijuana_plant_particle", FabricParticleTypes.simple(true));

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(DrugCraft.MOD_ID, name), particleType);
    }

    public static void registerParticles() {
        DrugCraft.LOGGER.info("Registering Particles for " + DrugCraft.MOD_ID);
    }
}
