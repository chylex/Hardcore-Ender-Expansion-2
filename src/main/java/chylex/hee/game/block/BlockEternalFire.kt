package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_IN_FIRE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ALL_PROTECTIONS
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.fx.util.playUniversal
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isTopSolid
import chylex.hee.game.world.util.removeBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Vec
import chylex.hee.util.random.nextFloat
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FireBlock
import net.minecraft.entity.Entity
import net.minecraft.particles.ParticleTypes.LARGE_SMOKE
import net.minecraft.util.Direction
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.world.GameRules.DO_FIRE_TICK
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import java.util.Random
import kotlin.math.max

class BlockEternalFire(builder: BlockBuilder) : FireBlock(builder.p), IHeeBlock {
	private companion object {
		private val PARTICLE_SMOKE = ParticleSpawnerVanilla(LARGE_SMOKE)
		private val DAMAGE_CONTACT = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS, FIRE_TYPE(12 * 20), RAPID_DAMAGE(5))
		
		private fun getModelVariations(): List<Pair<String, Int>> {
			return listOf("floor", "side", "side_alt", "up", "up_alt").flatMap { listOf(it to 0, it to 1) }
		}
	}
	
	override val model
		get() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi(getModelVariations().map { (name, alt) ->
				BlockModel.Suffixed("_$name$alt", BlockModel.WithTextures(
					BlockModel.FromParent(Blocks.FIRE.location("_$name$alt")),
					mapOf(
						"particle" to this.location("_$alt"),
						"fire" to this.location("_$alt"),
					)
				))
			})
		)
	
	override val renderLayer
		get() = CUTOUT
	
	override val drop
		get() = BlockDrop.Nothing
	
	init {
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun canDie(world: World, pos: BlockPos) = false
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		world.pendingBlockTicks.scheduleTick(pos, this, 2 * getTickCooldown(world.rand))
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		if (!world.gameRules.getBoolean(DO_FIRE_TICK) || !world.isAreaLoaded(pos, 2)) {
			return
		}
		
		if (!state.isValidPosition(world, pos)) {
			pos.removeBlock(world)
		}
		
		world.pendingBlockTicks.scheduleTick(pos, this, (2 * getTickCooldown(rand)) + rand.nextInt(10))
		
		val baseChance = if (world.isBlockinHighHumidity(pos))
			250
		else
			300
		
		for (facing in Facing6) {
			val offset = pos.offset(facing).takeIf { !world.isRainingAt(it) } ?: continue
			val chance = baseChance - (if (facing.yOffset == 0) 0 else 50)
			
			trySpread(world, offset, chance, rand, facing.opposite)
		}
	}
	
	private fun trySpread(world: World, pos: BlockPos, chance: Int, rand: Random, face: Direction) {
		val state = pos.getState(world)
		val block = state.block
		
		if (block === this || !state.isValidPosition(world, pos)) {
			return
		}
		
		var flammability = 0
		
		if (block.isAir(state, world, pos)) {
			for (neighborFacing in Facing6) {
				if (face != neighborFacing) {
					val neighborPos = pos.offset(neighborFacing)
					val neighborFlammability = neighborPos.getState(world).getFlammability(world, neighborPos, neighborFacing.opposite)
					
					flammability = max(flammability, neighborFlammability)
				}
			}
		}
		else {
			flammability = state.getFlammability(world, pos, face)
		}
		
		if (rand.nextInt(chance) < flammability) {
			if (block === ModBlocks.INFUSED_TNT) {
				state.catchFire(world, pos, face, null)
			}
			
			if (rand.nextInt(3) == 0 && !world.isRainingAt(pos)) {
				pos.setState(world, Blocks.FIRE.with(AGE, rand.nextInt(8, 12)))
			}
			
			if (block !== ModBlocks.INFUSED_TNT) {
				state.catchFire(world, pos, face, null)
			}
		}
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		DAMAGE_CONTACT.dealTo(1F, entity, TITLE_IN_FIRE)
	}
	
	@SubscribeEvent
	fun onLeftClickBlock(e: PlayerInteractEvent.LeftClickBlock) {
		val world = e.world
		val offsetPos = e.face?.let(e.pos::offset)
		
		if (offsetPos?.getBlock(world) === this) {
			SoundEvents.BLOCK_FIRE_EXTINGUISH.playUniversal(e.player, offsetPos, SoundCategory.BLOCKS, volume = 0.5F, pitch = world.rand.nextFloat(1.8F, 3.4F))
			offsetPos.removeBlock(world)
			e.isCanceled = true
		}
	}
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		if (rand.nextInt(30) == 0) {
			SoundEvents.BLOCK_FIRE_AMBIENT.playClient(pos, SoundCategory.BLOCKS, volume = rand.nextFloat(0.5F, 0.6F), pitch = rand.nextFloat(0.3F, 1F))
		}
		
		if (rand.nextInt(3) != 0) {
			if (pos.down().isTopSolid(world)) {
				PARTICLE_SMOKE.spawn(Point(Vec(
					pos.x + rand.nextFloat(0.0, 1.0),
					pos.y + rand.nextFloat(0.5, 1.0),
					pos.z + rand.nextFloat(0.0, 1.0)
				), 1), rand)
			}
			else {
				for (attempt in 1..4) {
					val facing = rand.nextItem(Facing6)
					
					if (canCatchFire(world, pos.offset(facing), facing.opposite)) {
						val perpendicular = facing.rotateY()
						
						val offsetFacing = rand.nextFloat(0.4, 0.5)
						val offsetSide = rand.nextFloat(-0.5, 0.5)
						
						PARTICLE_SMOKE.spawn(Point(Vec(
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
