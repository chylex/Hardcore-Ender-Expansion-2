package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos

abstract class ObsidianTowerLevel_Top(file: String) : ObsidianTowerLevel_General(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.setBlock(Pos(centerX, -1, 2), Blocks.OBSIDIAN)
		world.setBlock(Pos(centerX, -1, maxZ - 2), Blocks.OBSIDIAN)
		
		world.setBlock(Pos(2, -1, centerZ), Blocks.OBSIDIAN)
		world.setBlock(Pos(maxX - 2, -1, centerZ), Blocks.OBSIDIAN)
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
			world.addTrigger(Pos(centerX, 2, centerZ - 3), EntityStructureTrigger(::EntityBossEnderEye, yOffset = 0.0))
		}
	}
}
