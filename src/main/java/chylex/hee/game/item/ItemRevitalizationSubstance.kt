package chylex.hee.game.item
import chylex.hee.client.model.ModelHelper
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.mechanics.energy.IClusterHealth.HealthOverride.REVITALIZING
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.center
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playClient
import chylex.hee.system.util.playServer
import chylex.hee.system.util.readPos
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumActionResult.FAIL
import net.minecraft.util.EnumActionResult.SUCCESS
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class ItemRevitalizationSubstance : Item(){
	companion object{
		private val PARTICLE_FAIL = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(scale = 0.4F),
			pos = InBox(0.05F),
			mot = Gaussian(0.01F)
		)
		
		class FxUseData(private val pos: BlockPos, private val player: EntityPlayer, private val hand: EnumHand) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
				writeInt(player.entityId)
				writeByte(hand.ordinal)
			}
		}
		
		@JvmStatic
		val FX_FAIL = object : IFxHandler<FxUseData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val blockPos = readPos().center
				
				val player = world.getEntityByID(readInt()) as? EntityPlayer ?: return
				val hand = EnumHand.values().getOrNull(readByte().toInt()) ?: return
				
				val handPos = ModelHelper.getHandPosition(player, hand)
				val startPoint = handPos.add(handPos.directionTowards(blockPos).scale(0.2))
				
				PARTICLE_FAIL.spawn(Line(startPoint, blockPos, rand.nextFloat(0.4, 0.5)), rand)
				ModSounds.REVITALIZATION_SUBSTANCE_USE_FAIL.playClient(blockPos, SoundCategory.BLOCKS, volume = 0.9F)
			}
		}
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		if (world.isRemote){
			return FAIL // disable animation
		}
		
		val cluster = pos.getTile<TileEntityEnergyCluster>(world) ?: return FAIL
		
		if (cluster.currentHealth != REVITALIZING){
			if (cluster.addRevitalizationSubstance()){
				player.getHeldItem(hand).shrink(1)
				ModSounds.REVITALIZATION_SUBSTANCE_USE.playServer(world, pos, SoundCategory.BLOCKS, volume = 0.5F)
			}
			else{
				PacketClientFX(FX_FAIL, FxUseData(pos, player, hand)).sendToAllAround(player, 24.0)
			}
		}
		
		return SUCCESS
	}
}
