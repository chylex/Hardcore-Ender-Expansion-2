package chylex.hee.game.block
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.FLAG_NONE
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.translationKeyOriginal
import net.minecraft.block.BlockChorusFlower
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BlockChorusFlowerOverride : BlockChorusFlower(){
	init{
		val source = Blocks.CHORUS_FLOWER
		
		setHardness(source.getBlockHardness(null, null, null)) // UPDATE
		soundType = source.blockSoundType
		translationKey = source.translationKeyOriginal
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun canSurvive(world: World, pos: BlockPos): Boolean{
		val supportPos = pos.down()
		val supportBlock = supportPos.getBlock(world)
		
		if (supportBlock === Blocks.CHORUS_PLANT || supportBlock === ModBlocks.HUMUS){
			return true
		}
		else if (!supportPos.isAir(world)){
			return false
		}
		else{
			val adjacentPlants = Facing4.map(pos::offset).sumBy {
				when{
					it.getBlock(world) === Blocks.CHORUS_PLANT -> 1
					it.isAir(world) -> 0
					else -> return false
				}
			}
			
			return adjacentPlants == 1
		}
	}
	
	// Silently replace Humus below the flower with End Stone to bypass super.updateTick checks
	// UPDATE: replace this with ASM, probably
	
	@SubscribeEvent(priority = LOWEST)
	fun onCropGrowPre(e: CropGrowEvent.Pre){
		if (e.state.block === this){
			val world = e.world
			val supportingPos = e.pos.down(2) // the Pre event uses block above the actual plant
			
			if (supportingPos.getBlock(world) === ModBlocks.HUMUS){
				supportingPos.setBlock(world, Blocks.END_STONE, FLAG_NONE)
			}
		}
	}
	
	@SubscribeEvent(receiveCanceled = true) // TODO this will still not trigger if Pre is canceled, which is a serious issue but not during dev
	fun onCropGrowPost(e: CropGrowEvent.Post){
		if (e.originalState.block === this){
			val world = e.world
			val supportingPos = e.pos.down()
			
			if (supportingPos.getBlock(world) === Blocks.END_STONE){
				supportingPos.setBlock(world, ModBlocks.HUMUS, FLAG_NONE)
			}
		}
	}
}
