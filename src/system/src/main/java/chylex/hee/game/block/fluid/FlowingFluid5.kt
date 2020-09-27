package chylex.hee.game.block.fluid
import chylex.hee.system.migration.BlockFlowingFluid
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluids
import net.minecraft.fluid.IFluidState
import net.minecraftforge.fluids.ForgeFlowingFluid

class FlowingFluid5(properties: Properties) : ForgeFlowingFluid.Flowing(properties){
	companion object{
		const val FLOW_DISTANCE = 5
		
		fun stateToLevel(state: BlockState): Int{
			return when(state[BlockFlowingFluid.LEVEL]){
				in 0..3 -> 1
				      4 -> 2
				      5 -> 3
				      6 -> 4
				else    -> 5
			}
		}
	}
	
	override fun getFlowingFluidState(level: Int, falling: Boolean): IFluidState{
		return when{
			level == 1 -> Fluids.EMPTY.defaultState
			level <= 3 -> super.getFlowingFluidState(2, falling)
			else       -> super.getFlowingFluidState(level, falling)
		}
	}
}
