package chylex.hee.game.block
import chylex.hee.game.block.util.Property
import chylex.hee.game.fx.IFxData
import chylex.hee.game.fx.IFxHandler
import chylex.hee.game.particle.ParticleDeathFlowerHeal
import chylex.hee.game.particle.ParticleDeathFlowerHeal.Data
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.Gaussian
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.getState
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.readPos
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.setState
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockDeathFlowerDecaying : BlockEndPlant(){
	companion object{
		const val MIN_LEVEL = 1
		const val MAX_LEVEL = 14
		
		val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
		
		private val PARTICLE_POS = Constant(0.5F, DOWN) + InBox(-0.5F, 0.5F, 0F, BUSH_AABB.maxY.toFloat(), -0.5F, 0.5F)
		private val PARTICLE_MOT = Gaussian(0.02F)
		
		class FxHealData(private val pos: BlockPos, private val newLevel: Int) : IFxData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
				writeByte(newLevel)
			}
		}
		
		@JvmStatic
		val FX_HEAL = object : IFxHandler<FxHealData>{
			override fun handle(buffer: ByteBuf, world: World, rand: Random){
				val pos = buffer.readPos()
				val newLevel = buffer.readByte()
				
				val healLevel: Float
				val particleAmount: Int
				
				if (newLevel < MIN_LEVEL){
					healLevel = 1F
					particleAmount = 30
				}
				else{
					healLevel = 1F - ((1F + newLevel.toFloat() - MIN_LEVEL) / (1 + MAX_LEVEL - MIN_LEVEL)) // intentionally maps 13..1 to (0F)..(0.86F)
					particleAmount = 4
				}
				
				ParticleSpawnerCustom(
					type = ParticleDeathFlowerHeal,
					data = Data(healLevel),
					pos = PARTICLE_POS,
					mot = PARTICLE_MOT
				).spawn(Point(pos, particleAmount), rand)
			}
		}
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, LEVEL)
	
	// Healing
	
	fun healDeathFlower(world: World, pos: BlockPos){
		val state = pos.getState(world)
		val newDecayLevel = state.getValue(LEVEL) - world.rand.nextInt(1, 2)
		
		if (newDecayLevel < MIN_LEVEL){
			pos.setBlock(world, ModBlocks.DEATH_FLOWER_HEALED)
		}
		else{
			pos.setState(world, state.withProperty(LEVEL, newDecayLevel))
		}
		
		PacketClientFX(FX_HEAL, FxHealData(pos, newDecayLevel)).sendToAllAround(world, pos, 64.0)
	}
	
	// General
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(LEVEL) - MIN_LEVEL
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(LEVEL, meta + MIN_LEVEL)
	
	override fun damageDropped(state: IBlockState): Int{
		return state.getValue(LEVEL) - MIN_LEVEL
	}
}
