package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.behavior.EnderEyeSpawnerParticles
import chylex.hee.game.entity.posVec
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.OBSIDIAN_TOWER_TOP_GLOWSTONE
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBox
import chylex.hee.game.world.breakBlock
import chylex.hee.game.world.center
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.setAir
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.facades.Facing4
import chylex.hee.system.math.addY
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.EntityLightningBolt
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.use
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

abstract class ObsidianTowerLevel_Top(file: String) : ObsidianTowerLevel_General(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.setBlock(Pos(centerX, -1, 2), Blocks.OBSIDIAN)
		world.setBlock(Pos(centerX, -1, maxZ - 2), Blocks.OBSIDIAN)
		
		world.setBlock(Pos(2, -1, centerZ), Blocks.OBSIDIAN)
		world.setBlock(Pos(maxX - 2, -1, centerZ), Blocks.OBSIDIAN)
		
		for(facing in Facing4){
			world.addTrigger(Pos(centerX, 13, centerZ).offset(facing, 9), EntityStructureTrigger(OBSIDIAN_TOWER_TOP_GLOWSTONE))
		}
	}
	
	class Token(file: String, private val tokenType: TokenType, private val territoryType: TerritoryType) : ObsidianTowerLevel_Top(file){
		override fun generate(world: IStructureWorld, instance: Instance){
			super.generate(world, instance)
			world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, tokenType, territoryType) }, yOffset = 0.65))
		}
	}
	
	class Boss(file: String) : ObsidianTowerLevel_Top(file){
		override fun generate(world: IStructureWorld, instance: Instance){
			super.generate(world, instance)
			world.setBlock(Pos(centerX, 1, centerZ - 3), ModBlocks.OBSIDIAN_CHISELED_LIT)
			world.addTrigger(Pos(centerX, 2, centerZ - 3), PlaceholderTrigger())
		}
		
		class PlaceholderTrigger : IStructureTrigger{
			override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){}
			override fun realize(world: IWorld, pos: BlockPos, transform: Transform){}
		}
	}
	
	class GlowstoneTrigger : ITriggerHandler{
		override fun check(world: World) = false
		override fun update(entity: EntityTechnicalTrigger){}
		override fun nextTimer(rand: Random) = Int.MAX_VALUE
	}
	
	class DeathAnimationTrigger : ITriggerHandler{
		private companion object{
			private const val STAGE_TAG = "Stage"
			private const val CHARGE_ANIM_TAG = "ChargeAnim"
			
			private const val STAGE_WAIT = 0
			private const val STAGE_LIGHTNING = 1
			private const val STAGE_CHARGE_PARTICLES = 2
			private const val STAGE_CHARGE_TOKEN = 3
		}
		
		private var stage = 0
		private var nextTimer = 0
		private var chargeAnim = 0F
		
		override fun check(world: World): Boolean{
			return !world.isRemote
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			if (stage == STAGE_WAIT){
				nextTimer = 35
				++stage
				return
			}
			
			val world = entity.world as ServerWorld
			val pos = Pos(entity)
			
			if (stage == STAGE_LIGHTNING){
				world.addLightningBolt(EntityLightningBolt(world, entity.posX, entity.posY + 0.49, entity.posZ, true))
				
				for(testPos in pos.allInCenteredBox(4, 0, 4)){
					if (testPos.getBlock(world) === Blocks.OBSIDIAN){
						testPos.breakBlock(world, false)
						
						if (abs(testPos.x - pos.x) != 4 && abs(testPos.z - pos.z) != 4){
							testPos.down().takeIf { it.getBlock(world) === ModBlocks.OBSIDIAN_SMOOTH }?.setAir(world)
						}
					}
				}
				
				nextTimer = 30
				++stage
			}
			else if (stage == STAGE_CHARGE_PARTICLES || stage == STAGE_CHARGE_TOKEN){ // TODO demon eye drops
				val tokenHolderPos = pos.down(6)
				val tokenHolder = world.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(tokenHolderPos)).firstOrNull()
				
				if (tokenHolder != null){
					if (stage == STAGE_CHARGE_PARTICLES){
						val charge = min(1F, chargeAnim + (1F / 140F))
						chargeAnim = charge
						
						for(facing in Facing4){
							val start = pos.offset(facing, 9).up(13)
							
							val progressCurvePoint = when{
								chargeAnim < 0.2 -> sqrt(chargeAnim / 0.2)
								chargeAnim < 0.3 -> 1.0
								else             -> 1.0 - ((chargeAnim - 0.3) / 0.7)
							}
							
							val offsetProgress = chargeAnim.pow(0.8F).toDouble()
							val particlePos = start.center.offsetTowards(tokenHolder.posVec.addY(tokenHolder.height * 0.5), offsetProgress).addY(progressCurvePoint * 6.0)
							val particleData = EnderEyeSpawnerParticles.Companion.ParticleData(particlePos)
							
							PacketClientFX(EnderEyeSpawnerParticles.FX_PARTICLE, particleData).sendToAllAround(entity.world, pos, 256.0)
						}
						
						if (chargeAnim == 1F){
							++stage
						}
					}
					
					if (chargeAnim > 0.96F){
						val charge = min(1F, tokenHolder.currentCharge + (1F / 23F))
						tokenHolder.currentCharge = charge
						
						if (charge == 1F){
							++stage
						}
					}
				}
			}
			else{
				entity.remove()
			}
		}
		
		override fun nextTimer(rand: Random): Int{
			val timer = nextTimer
			nextTimer = 0
			return timer
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putByte(STAGE_TAG, stage.toByte())
			putFloat(CHARGE_ANIM_TAG, chargeAnim)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			stage = getByte(STAGE_TAG).toInt()
			chargeAnim = getFloat(CHARGE_ANIM_TAG)
		}
	}
}
