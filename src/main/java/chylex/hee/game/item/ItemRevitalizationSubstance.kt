package chylex.hee.game.item

import chylex.hee.client.model.util.ModelHelper
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.entity.item.EntityItemRevitalizationSubstance
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.IItemEntityComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.util.buffer.readPos
import chylex.hee.util.buffer.use
import chylex.hee.util.buffer.writePos
import chylex.hee.util.math.center
import chylex.hee.util.math.directionTowards
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

object ItemRevitalizationSubstance : HeeItemBuilder() {
	init {
		maxStackSize = 16
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (world.isRemote) {
					return FAIL // disable animation
				}
				
				val cluster = pos.getTile<TileEntityEnergyCluster>(world) ?: return FAIL
				if (cluster.currentHealth != REVITALIZING) {
					if (cluster.addRevitalizationSubstance()) {
						player.getHeldItem(context.hand).shrink(1)
						ModSounds.ITEM_REVITALIZATION_SUBSTANCE_USE_SUCCESS.playServer(world, pos, SoundCategory.BLOCKS, volume = 0.5F)
					}
					else {
						PacketClientFX(FX_FAIL, FxUseData(pos, player, context.hand)).sendToAllAround(player, 24.0)
					}
				}
				
				return SUCCESS
			}
		}
		
		components.itemEntity = IItemEntityComponent.fromConstructor(::EntityItemRevitalizationSubstance)
	}
	
	private val PARTICLE_FAIL = ParticleSpawnerCustom(
		type = ParticleSmokeCustom,
		data = ParticleSmokeCustom.Data(scale = 0.4F),
		pos = InBox(0.05F),
		mot = Gaussian(0.01F)
	)
	
	class FxUseData(private val pos: BlockPos, private val player: PlayerEntity, private val hand: Hand) : IFxData {
		override fun write(buffer: PacketBuffer) = buffer.use {
			writePos(pos)
			writeInt(player.entityId)
			writeByte(hand.ordinal)
		}
	}
	
	val FX_FAIL = object : IFxHandler<FxUseData> {
		override fun handle(buffer: PacketBuffer, world: World, rand: Random) = buffer.use {
			val blockPos = readPos().center
			
			val player = world.getEntityByID(readInt()) as? PlayerEntity ?: return
			val hand = Hand.values().getOrNull(readByte().toInt()) ?: return
			
			val handPos = ModelHelper.getHandPosition(player, hand)
			val startPoint = handPos.add(handPos.directionTowards(blockPos).scale(0.2))
			
			PARTICLE_FAIL.spawn(Line(startPoint, blockPos, rand.nextFloat(0.4, 0.5)), rand)
			ModSounds.ITEM_REVITALIZATION_SUBSTANCE_USE_FAIL.playClient(blockPos, SoundCategory.BLOCKS, volume = 0.9F)
		}
	}
}
