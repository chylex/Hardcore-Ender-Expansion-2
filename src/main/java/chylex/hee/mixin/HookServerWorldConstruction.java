package chylex.hee.mixin;

import chylex.hee.HEE;
import chylex.hee.game.world.ServerWorldEndCustom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class HookServerWorldConstruction {
	@Redirect(method = "func_240787_a_", at = @At(value = "NEW", target = "net/minecraft/world/server/ServerWorld", ordinal = 1), require = 1)
	private ServerWorld constructEndWorld(final MinecraftServer server, final Executor backgroundExecutor, final LevelSave levelSave, final IServerWorldInfo serverWorldInfo, final RegistryKey<World> dimension, final DimensionType dimensionType, final IChunkStatusListener statusListener, final ChunkGenerator chunkGenerator, final boolean isDebug, final long seed, final List<ISpecialSpawner> specialSpawners, final boolean shouldBeTicking) {
		if (dimension == HEE.INSTANCE.getDim()) {
			return new ServerWorldEndCustom(server, backgroundExecutor, levelSave, serverWorldInfo, dimension, dimensionType, statusListener, chunkGenerator, isDebug, seed, specialSpawners, shouldBeTicking);
		}
		else {
			return new ServerWorld(server, backgroundExecutor, levelSave, serverWorldInfo, dimension, dimensionType, statusListener, chunkGenerator, isDebug, seed, specialSpawners, shouldBeTicking);
		}
	}
}
