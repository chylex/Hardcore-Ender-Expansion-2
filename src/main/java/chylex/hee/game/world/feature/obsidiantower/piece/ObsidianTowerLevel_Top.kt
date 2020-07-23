package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.OBSIDIAN_TOWER_TOP_GLOWSTONE
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.facades.Facing4
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World
import java.util.Random

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
}
