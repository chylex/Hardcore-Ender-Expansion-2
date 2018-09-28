package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHardnessWithResistance
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.item.util.Teleporter
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.readPos
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setState
import chylex.hee.system.util.use
import chylex.hee.system.util.writePos
import io.netty.buffer.ByteBuf
import net.minecraft.block.BlockDragonEgg
import net.minecraft.block.BlockFalling
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import java.util.Random

class BlockDragonEggOverride : BlockDragonEgg(){
	companion object{
		private val PARTICLE_BREAK = ParticleSpawnerCustom(
			type = ParticleTeleport,
			data = ParticleTeleport.Data(minLifespan = 7, maxLifespan = 14, minScale = 2.7F, maxScale = 3.4F),
			pos = InBox(0.66F),
			mot = InBox(0.025F)
		)
		
		class FxBreakData(private val pos: BlockPos) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writePos(pos)
			}
		}
		
		@JvmStatic
		val FX_BREAK = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val pos = readPos()
				val sound = Blocks.DRAGON_EGG.soundType
				
				PARTICLE_BREAK.spawn(Point(pos, 30), rand)
				world.playSound(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, sound.breakSound, SoundCategory.BLOCKS, (sound.getVolume() + 1F) / 2F, sound.getPitch() * 0.8F, false)
			}
		}
	}
	
	init{
		val source = Blocks.DRAGON_EGG
		
		setHardnessWithResistance(INDESTRUCTIBLE_HARDNESS, 15F)
		lightValue = 2
		soundType = source.soundType
		unlocalizedName = source.unlocalizedName.removePrefix("tile.") // UPDATE: there must be a better way?
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		if (world.isRemote || BlockFalling.fallInstantly){ // since the Egg is never used in worldgen, it's easier to just ignore fallInstantly
			return
		}
		
		if (EntityFallingBlockHeavy.canFallThrough(world, pos.down()) && pos.y >= 0){
			world.spawnEntity(EntityFallingBlockHeavy(world, pos, defaultState))
		}
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		val realState = pos.getState(world)
		
		if (world.isRemote || realState.block != this){
			return true
		}
		
		val rand = world.rand
		
		repeat(500){
			val targetPos = pos.add(
				rand.nextInt(-15, 15),
				rand.nextInt(-3, 7),
				rand.nextInt(-15, 15)
			)
			
			if (targetPos.isAir(world) && targetPos.y > 0 && world.getHeight(targetPos.x, targetPos.z) > 0){
				val solidPos = targetPos.offsetUntil(DOWN, 1..(targetPos.y)){ it.blocksMovement(world) }
				
				if (solidPos != null){
					val finalPos = if (targetPos.y - solidPos.y <= 7) solidPos.up() else targetPos
					
					finalPos.setState(world, realState)
					pos.setAir(world)
					
					Teleporter.sendTeleportFX(world, pos.center, finalPos.center, SoundCategory.BLOCKS, 0.7F)
					return true
				}
			}
		}
		
		return true
	}
	
	override fun onBlockClicked(world: World, pos: BlockPos, player: EntityPlayer){
		val state = pos.getState(world)
		
		if (world.isRemote || state.block != this){
			return
		}
		
		pos.setAir(world)
		PacketClientFX(FX_BREAK, FxBreakData(pos)).sendToAllAround(world, pos, 32.0)
		
		if (world.gameRules.getBoolean("doTileDrops") && !world.restoringBlockSnapshots){
			EntityItem(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(this)).apply {
				motionVec = Vec3d.ZERO
				setDefaultPickupDelay()
				world.spawnEntity(this)
			}
		}
	}
}
