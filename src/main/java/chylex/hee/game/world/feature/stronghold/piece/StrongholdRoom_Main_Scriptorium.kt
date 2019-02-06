package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.FlowerPotStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST

class StrongholdRoom_Main_Scriptorium(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.addTrigger(Pos(centerX, 2, 1), FlowerPotStructureTrigger(ItemStack(Blocks.DEADBUSH)))
		
		repeat(rand.nextInt(7, 12)){
			for(attempt in 1..50){
				val testPos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, maxZ - 1)).offsetUntil(UP, 0 until maxY){ world.isAir(it) }
				
				if (testPos == null || (testPos.y == 1 && rand.nextInt(4) != 0)){
					continue
				}
				
				val isInAir = testPos.y > 1
				
				if (world.isAir(testPos) && (isInAir || Facing4.any { !world.isAir(testPos.offset(it)) })){
					val below = world.getBlock(testPos.down())
					
					if (below === Blocks.BOOKSHELF || below === Blocks.STONEBRICK || below === Blocks.MONSTER_EGG || below === Blocks.STONE_SLAB){
						if (isInAir && rand.nextInt(4) == 0 && world.isAir(testPos.up())){
							world.setBlock(testPos.up(), ModBlocks.ANCIENT_COBWEB)
						}
						else{
							world.setBlock(testPos, ModBlocks.ANCIENT_COBWEB)
						}
						
						break
					}
				}
			}
		}
	}
	
	// TODO add a chest somewhere?
	// TODO maybe add more torches
}
