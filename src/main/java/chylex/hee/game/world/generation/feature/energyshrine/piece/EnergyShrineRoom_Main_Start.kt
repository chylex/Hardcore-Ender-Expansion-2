package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.Environment
import chylex.hee.game.block.BlockGloomrock
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.ENERGY_SHRINE_GLOBAL
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.particle.ParticleGlitter
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Gaussian
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.util.color.IColorGenerator
import chylex.hee.util.color.RGB
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.world.World
import java.util.Random

class EnergyShrineRoom_Main_Start(file: String) : EnergyShrineRoom_Generic(file) {
	private companion object {
		private const val PARTICLE_DISTANCE = 18
		private val PARTICLE_GLOOMROCK_CHECK_RANGE = 1..15
		
		private val PARTICLE_GLITTER = ParticleSpawnerCustom(
			type = ParticleGlitter,
			data = ParticleGlitter.Data(color = IColorGenerator { RGB(nextInt(112, 212), 128, nextInt(160, 240)) }, maxAgeMultiplier = 3..7),
			pos = InBox(0.5F),
			mot = Gaussian(0.0025F),
			maxRange = PARTICLE_DISTANCE + 2.0
		)
		
		private val RAND = Random()
	}
	
	object Particles : ITriggerHandler {
		override fun check(world: World): Boolean {
			return world.isRemote
		}
		
		override fun update(entity: EntityTechnicalTrigger) {
			val player = Environment.getClientSidePlayer() ?: return
			val box = EnergyShrinePieces.STRUCTURE_SIZE.toCenteredBoundingBox(entity.posVec)
			
			if (!box.contains(player.posVec)) {
				return
			}
			
			val world = player.world
			val playerPos = Pos(player.posVec)
			
			for (attempt in 1..250) {
				val targetPos = playerPos.add(
					RAND.nextInt(-PARTICLE_DISTANCE, PARTICLE_DISTANCE),
					RAND.nextInt(-PARTICLE_DISTANCE / 3, PARTICLE_DISTANCE / 3),
					RAND.nextInt(-PARTICLE_DISTANCE, PARTICLE_DISTANCE)
				)
				
				if (targetPos.getState(world).isOpaqueCube(world, targetPos)) {
					continue
				}
				
				if (targetPos.offsetUntil(UP, PARTICLE_GLOOMROCK_CHECK_RANGE) { it.getBlock(world) is BlockGloomrock } == null) {
					continue
				}
				
				if (targetPos.offsetUntil(DOWN, PARTICLE_GLOOMROCK_CHECK_RANGE) { it.getBlock(world) is BlockGloomrock } == null) {
					continue
				}
				
				PARTICLE_GLITTER.spawn(Point(targetPos, 1), RAND)
			}
		}
		
		override fun nextTimer(rand: Random): Int {
			return 3
		}
	}
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, maxY - EnergyShrinePieces.STRUCTURE_SIZE.centerY, centerZ), EntityStructureTrigger(ENERGY_SHRINE_GLOBAL))
	}
}
