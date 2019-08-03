package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.playClient
import chylex.hee.system.util.playUniversal
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.BlockFire
import net.minecraft.block.BlockTNT
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes.SMOKE_LARGE
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.max

class BlockEternalFire(builder: BlockBuilder) : BlockFire(){
	private companion object{
		private val PARTICLE_SMOKE = ParticleSpawnerVanilla(SMOKE_LARGE)
		private val DAMAGE_CONTACT = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS, FIRE_TYPE(12 * 20), RAPID_DAMAGE(5))
	}
	
	init{
		setupBlockProperties(builder)
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun getMetaFromState(state: IBlockState) = 0
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState
	
	override fun tickRate(world: World) = super.tickRate(world) * 2
	override fun canDie(world: World, pos: BlockPos) = false
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		if (!world.gameRules.getBoolean("doFireTick") || !world.isAreaLoaded(pos, 2)){
			return
		}
		
		if (!canPlaceBlockAt(world, pos)){
			world.setBlockToAir(pos)
		}
		
		world.scheduleUpdate(pos, this, tickRate(world) + rand.nextInt(10))
		
		val baseChance = if (world.isBlockinHighHumidity(pos))
			250
		else
			300
		
		for(facing in Facing6){
			val offset = pos.offset(facing).takeIf { !world.isRainingAt(it) } ?: continue
			val chance = baseChance - (if (facing.yOffset == 0) 0 else 50)
			
			trySpread(world, offset, chance, rand, facing.opposite)
		}
	}
	
	private fun trySpread(world: World, pos: BlockPos, chance: Int, rand: Random, face: EnumFacing){
		val state = pos.getState(world)
		val block = state.block
		
		if (block === this || !canPlaceBlockAt(world, pos)){
			return
		}
		
		var flammability = 0
		
		if (block.isAir(state, world, pos)){
			for(neighborFacing in Facing6){
				if (face != neighborFacing){
					val neighborPos = pos.offset(neighborFacing)
					val neighborFlammability = neighborPos.getBlock(world).getFlammability(world, neighborPos, neighborFacing.opposite)
					
					flammability = max(flammability, neighborFlammability)
				}
			}
		}
		else{
			flammability = block.getFlammability(world, pos, face)
		}
		
		if (rand.nextInt(chance) < flammability){
			if (rand.nextInt(3) == 0 && !world.isRainingAt(pos)){
				pos.setState(world, Blocks.FIRE.defaultState.with(AGE, rand.nextInt(8, 12)))
			}
			
			if (block === Blocks.TNT){
				Blocks.TNT.onPlayerDestroy(world, pos, state.with(BlockTNT.EXPLODE, true))
			}
		}
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		DAMAGE_CONTACT.dealTo(1F, entity, Damage.TITLE_IN_FIRE)
	}
	
	@SubscribeEvent
	fun onLeftClickBlock(e: PlayerInteractEvent.LeftClickBlock){
		val world = e.world
		val offsetPos = e.face?.let(e.pos::offset)
		
		if (offsetPos?.getBlock(world) === this){
			SoundEvents.BLOCK_FIRE_EXTINGUISH.playUniversal(e.entityPlayer, offsetPos, SoundCategory.BLOCKS, volume = 0.5F, pitch = world.rand.nextFloat(1.8F, 3.4F))
			offsetPos.setAir(world)
			e.isCanceled = true
		}
	}
	
	@SideOnly(Side.CLIENT)
	override fun randomDisplayTick(state: IBlockState, world: World, pos: BlockPos, rand: Random){
		if (rand.nextInt(30) == 0){
			SoundEvents.BLOCK_FIRE_AMBIENT.playClient(pos, SoundCategory.BLOCKS, volume = rand.nextFloat(0.5F, 0.6F), pitch = rand.nextFloat(0.3F, 1F))
		}
		
		if (rand.nextInt(3) != 0){
			if (pos.down().isTopSolid(world)){
				PARTICLE_SMOKE.spawn(Point(Vec3d(
					pos.x + rand.nextFloat(0.0, 1.0),
					pos.y + rand.nextFloat(0.5, 1.0),
					pos.z + rand.nextFloat(0.0, 1.0)
				), 1), rand)
			}
			else{
				for(attempt in 1..4){
					val facing = rand.nextItem(Facing6)
					
					if (canCatchFire(world, pos.offset(facing), facing.opposite)){
						val perpendicular = facing.rotateY()
						
						val offsetFacing = rand.nextFloat(0.4, 0.5)
						val offsetSide = rand.nextFloat(-0.5, 0.5)
						
						PARTICLE_SMOKE.spawn(Point(Vec3d(
							pos.x + 0.5 + (facing.xOffset * offsetFacing) + (perpendicular.xOffset * offsetSide),
							pos.y + rand.nextFloat(0.0, 1.0),
							pos.z + 0.5 + (facing.zOffset * offsetFacing) + (perpendicular.zOffset * offsetSide)
						), 1), rand)
						
						break
					}
				}
			}
		}
	}
}
