package chylex.hee.game.block
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.Teleporter.Companion.FxTeleportData
import chylex.hee.game.entity.item.EntityFallingBlockHeavy
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.blocksMovement
import chylex.hee.game.world.center
import chylex.hee.game.world.getState
import chylex.hee.game.world.isAir
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.playClient
import chylex.hee.game.world.removeBlock
import chylex.hee.game.world.setState
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxBlockHandler
import chylex.hee.system.math.Vec3
import chylex.hee.system.migration.BlockDragonEgg
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.random.nextInt
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.GameRules.DO_TILE_DROPS
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type.OCEAN_FLOOR
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockDragonEggOverride(builder: BlockBuilder) : BlockDragonEgg(builder.p){
	companion object{
		private val PARTICLE_BREAK = ParticleSpawnerCustom(
			type = ParticleTeleport,
			data = ParticleTeleport.Data(lifespan = 7..14, scale = (2.7F)..(3.4F)),
			pos = InBox(0.66F),
			mot = InBox(0.025F)
		)
		
		val FX_BREAK = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_BREAK.spawn(Point(pos, 30), rand)
				
				Blocks.DRAGON_EGG.defaultState.getSoundType(world, pos, null).let {
					it.breakSound.playClient(pos, SoundCategory.BLOCKS, volume = (it.getVolume() + 1F) / 2F, pitch = it.getPitch() * 0.8F)
				}
			}
		}
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		if (world.isRemote){
			return
		}
		
		if (EntityFallingBlockHeavy.canFallThrough(world, pos.down()) && pos.y >= 0){
			world.addEntity(EntityFallingBlockHeavy(world, pos, defaultState))
		}
	}
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer, hand: Hand, hit: BlockRayTraceResult): ActionResultType{
		val realState = pos.getState(world)
		
		if (world.isRemote || realState.block !== this){
			return SUCCESS
		}
		
		val rand = world.rand
		
		repeat(500){
			val targetPos = pos.add(
				rand.nextInt(-15, 15),
				rand.nextInt(-3, 7),
				rand.nextInt(-15, 15)
			)
			
			if (targetPos.isAir(world) && targetPos.y > 0 && world.getHeight(OCEAN_FLOOR, targetPos).y > 0){
				val solidPos = targetPos.offsetUntil(DOWN, 1..(targetPos.y)){ it.blocksMovement(world) }
				
				if (solidPos != null){
					val finalPos = if (targetPos.y - solidPos.y <= 7) solidPos.up() else targetPos
					
					finalPos.setState(world, realState)
					pos.removeBlock(world)
					
					FxTeleportData(
						startPoint = pos.center,
						endPoint = finalPos.center,
						width = 0.2F,
						height = 0.2F,
						soundEvent = ModSounds.ENTITY_GENERIC_TELEPORT,
						soundCategory = SoundCategory.BLOCKS,
						soundVolume = 0.7F
					).send(world)
					
					return SUCCESS
				}
			}
		}
		
		return SUCCESS
	}
	
	override fun onBlockClicked(state: BlockState, world: World, pos: BlockPos, player: EntityPlayer){
		if (world.isRemote || state.block !== this){
			return
		}
		
		pos.removeBlock(world)
		PacketClientFX(FX_BREAK, FxBlockData(pos)).sendToAllAround(world, pos, 32.0)
		
		if (world.gameRules.getBoolean(DO_TILE_DROPS) && !world.restoringBlockSnapshots){
			EntityItem(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(this)).apply {
				motion = Vec3.ZERO
				setDefaultPickupDelay()
				world.addEntity(this)
			}
		}
	}
}
