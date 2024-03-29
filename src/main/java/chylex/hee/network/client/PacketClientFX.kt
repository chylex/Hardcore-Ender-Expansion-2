package chylex.hee.network.client

import chylex.hee.HEE
import chylex.hee.game.block.BlockDragonEggOverride
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.block.BlockPuzzleLogic
import chylex.hee.game.block.IBlockDeathFlowerDecaying
import chylex.hee.game.block.entity.TileEntityBrewingStandCustom
import chylex.hee.game.block.entity.TileEntityExperienceGate
import chylex.hee.game.block.entity.TileEntityMinersBurialAltar
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.block.entity.base.TileEntityBaseSpawner
import chylex.hee.game.entity.Teleporter
import chylex.hee.game.entity.item.EntityFallingObsidian
import chylex.hee.game.entity.item.EntityItemCauldronTrigger
import chylex.hee.game.entity.item.EntityItemIgneousRock
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.living.EntityMobUndread
import chylex.hee.game.entity.living.behavior.EnderEyePhase
import chylex.hee.game.entity.living.behavior.EnderEyeSpawnerParticles
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.living.behavior.UndreadDustEffects
import chylex.hee.game.entity.projectile.EntityProjectileSpatialDash
import chylex.hee.game.entity.technical.EntityTechnicalIgneousPlateLogic
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.item.ItemAbstractEnergyUser
import chylex.hee.game.item.ItemCompost
import chylex.hee.game.item.ItemRevitalizationSubstance
import chylex.hee.game.item.ItemTableLink
import chylex.hee.game.mechanics.scorching.ScorchingHelper
import chylex.hee.game.mechanics.table.TableParticleHandler
import chylex.hee.game.potion.BanishmentEffect
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonRoom_Tomb
import chylex.hee.network.BaseClientPacket
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import io.netty.buffer.ByteBuf
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.network.PacketBuffer
import java.util.Random

class PacketClientFX<T : IFxData>() : BaseClientPacket() {
	private companion object {
		private val RAND = Random()
		
		private val HANDLERS = arrayOf(
			Teleporter.FX_TELEPORT,
			ScorchingHelper.FX_BLOCK_BREAK,
			ScorchingHelper.FX_ENTITY_HIT,
			TableParticleHandler.FX_PROCESS_PEDESTALS,
			TableParticleHandler.FX_DRAIN_CLUSTER,
			IBlockDeathFlowerDecaying.FX_HEAL,
			IBlockDeathFlowerDecaying.FX_WITHER,
			BlockDragonEggOverride.FX_BREAK,
			BlockEnderGooPurified.FX_PLACE,
			BlockPuzzleLogic.FX_TOGGLE,
			BlockPuzzleLogic.FX_SOLVE_TOGGLE,
			BlockPuzzleLogic.FX_SOLVE_SPAWN,
			TileEntityBaseSpawner.FX_TAINT_TICK,
			TileEntityBrewingStandCustom.FX_AMELIORATE,
			TileEntityExperienceGate.FX_CONSUME,
			TileEntityMinersBurialAltar.FX_SPAWN,
			TileEntitySpawnerObsidianTower.FX_BREAK,
			TileEntityTablePedestal.FX_ITEM_UPDATE,
			ItemAbstractEnergyUser.FX_CHARGE,
			ItemCompost.FX_USE,
			ItemRevitalizationSubstance.FX_FAIL,
			ItemTableLink.FX_USE,
			EntityFallingObsidian.FX_FALL,
			EntityItemCauldronTrigger.FX_RECIPE_FINISH,
			EntityItemIgneousRock.FX_BLOCK_SMELT,
			EntityItemIgneousRock.FX_ENTITY_BURN,
			EntityItemRevitalizationSubstance.FX_REVITALIZE_GOO,
			EntityMobSilverfish.FX_SPAWN_PARTICLE,
			EntityMobUndread.FX_END_DISAPPEAR,
			EntityProjectileSpatialDash.FX_EXPIRE,
			EntityTechnicalIgneousPlateLogic.FX_COOLING,
			EntityTechnicalIgneousPlateLogic.FX_OVERHEAT,
			EntityTokenHolder.FX_BREAK,
			EnderEyePhase.Floating.FX_FINISH,
			EnderEyeSpawnerParticles.FX_PARTICLE,
			EndermanTeleportHandler.FX_TELEPORT_FAIL,
			EndermanTeleportHandler.FX_TELEPORT_OUT_OF_WORLD,
			BanishmentEffect.FX_BANISH,
			UndreadDustEffects.FX_CURSE,
			TombDungeonRoom_Tomb.MobSpawnerTrigger.FX_SPAWN_UNDREAD,
			TombDungeonRoom_Tomb.MobSpawnerTrigger.FX_SPAWN_SPIDERLING,
		)
	}
	
	// Instance
	
	constructor(handler: IFxHandler<T>, data: T) : this() {
		this.handler = handler
		this.data = data
	}
	
	private lateinit var handler: IFxHandler<T>
	private lateinit var data: IFxData
	
	private var buffer: ByteBuf? = null
	
	override fun write(buffer: PacketBuffer) {
		buffer.writeByte(HANDLERS.indexOf(handler))
		data.write(buffer)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun read(buffer: PacketBuffer) {
		val index = buffer.readByte().toInt()
		
		if (index == -1) {
			if (HEE.debug) {
				throw IndexOutOfBoundsException("could not find FX handler")
			}
		}
		else {
			this.handler = HANDLERS[index] as IFxHandler<T>
			this.buffer = buffer.slice()
		}
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: ClientPlayerEntity) {
		buffer?.let { handler.handle(PacketBuffer(it), player.world, RAND) }
	}
}
