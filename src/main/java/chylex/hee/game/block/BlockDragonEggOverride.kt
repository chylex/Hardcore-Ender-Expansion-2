package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHardnessWithResistance
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.game.world.util.Teleporter.Companion.FxTeleportData
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.center
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.playClient
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setState
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
import java.util.Random

class BlockDragonEggOverride : BlockDragonEgg(){
	companion object{
		private val PARTICLE_BREAK = ParticleSpawnerCustom(
			type = ParticleTeleport,
			data = ParticleTeleport.Data(minLifespan = 7, maxLifespan = 14, minScale = 2.7F, maxScale = 3.4F),
			pos = InBox(0.66F),
			mot = InBox(0.025F)
		)
		
		@JvmStatic
		val FX_BREAK = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_BREAK.spawn(Point(pos, 30), rand)
				
				Blocks.DRAGON_EGG.soundType.let {
					it.breakSound.playClient(pos, SoundCategory.BLOCKS, volume = (it.getVolume() + 1F) / 2F, pitch = it.getPitch() * 0.8F)
				}
			}
		}
	}
	
	init{
		val source = Blocks.DRAGON_EGG
		
		setHardnessWithResistance(INDESTRUCTIBLE_HARDNESS, 15F)
		lightValue = 2
		soundType = source.soundType
		translationKey = source.translationKey.removePrefix("tile.") // UPDATE: there must be a better way?
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
		
		if (world.isRemote || realState.block !== this){
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
					
					FxTeleportData(
						startPoint = pos.center,
						endPoint = finalPos.center,
						width = 0.2F,
						height = 0.2F,
						soundCategory = SoundCategory.BLOCKS,
						soundVolume = 0.7F
					).send(world)
					
					return true
				}
			}
		}
		
		return true
	}
	
	override fun onBlockClicked(world: World, pos: BlockPos, player: EntityPlayer){
		val state = pos.getState(world)
		
		if (world.isRemote || state.block !== this){
			return
		}
		
		pos.setAir(world)
		PacketClientFX(FX_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
		
		if (world.gameRules.getBoolean("doTileDrops") && !world.restoringBlockSnapshots){
			EntityItem(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(this)).apply {
				motionVec = Vec3d.ZERO
				setDefaultPickupDelay()
				world.spawnEntity(this)
			}
		}
	}
}
