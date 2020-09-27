package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.entity.base.TileEntityBaseTableWithSupportingItem.Companion.SUPPORTING_ITEM_MAPPINGS
import chylex.hee.game.inventory.size
import chylex.hee.game.item.infusion.Infusion
import chylex.hee.game.item.infusion.InfusionRecipe
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer.Companion.CONSUME_ONE
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Output
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.mechanics.table.process.ProcessSupportingItemHolder
import chylex.hee.game.mechanics.table.process.serializer.MultiProcessSerializer
import chylex.hee.game.mechanics.table.process.serializer.MultiProcessSerializer.Companion.Mapping
import chylex.hee.init.ModTileEntities
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.math.over
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.putEnum
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos

class TileEntityInfusionTable(type: TileEntityType<TileEntityInfusionTable>) : TileEntityBaseTable(type){
	@Suppress("unused")
	constructor() : this(ModTileEntities.INFUSION_TABLE)
	
	override val tableIndicatorColor = RGB(45, 139, 184)
	override val tableDustType = DustType.END_POWDER
	
	override val processTickRate = 1
	override val processSerializer = MultiProcessSerializer(
		*SUPPORTING_ITEM_MAPPINGS, Mapping("", ::Process)
	)
	
	override fun createNewProcesses(unassignedPedestals: List<TileEntityTablePedestal>): List<ITableProcess>{
		val newProcesses = ArrayList<ITableProcess>(1)
		
		while(true){
			val process = tryAssignProcess(unassignedPedestals.filter { it.hasInputItem && checkNotUsed(newProcesses, it) })
			
			if (process == null){
				break
			}
			else{
				newProcesses.add(process)
			}
		}
		
		for(pedestal in unassignedPedestals){
			if (pedestal.hasInputItem && checkNotUsed(newProcesses, pedestal)){
				val stack = pedestal.itemInputCopy
				
				if (Infusion.isInfusable(stack.item) && currentProcessList.any { it is Process && it.infusion.tryInfuse(stack) != null }){
					newProcesses.add(ProcessSupportingItemHolder(this, pedestal.pos))
				}
			}
		}
		
		return newProcesses
	}
	
	private fun checkNotUsed(processes: List<ITableProcess>, pedestal: TileEntityTablePedestal): Boolean{
		return processes.none { it.pedestals.contains(pedestal.pos) }
	}
	
	private fun tryAssignProcess(ingredientPedestals: List<TileEntityTablePedestal>): ITableProcess?{
		for(recipe in InfusionRecipe.values()){
			val ingredients = recipe.ingredients
			
			if (ingredientPedestals.size >= ingredients.size){
				val usedPedestals = ArrayList<BlockPos>(ingredients.size)
				val remainingIngredients = ingredients.toMutableList()
				
				for(pedestal in ingredientPedestals){
					val index = remainingIngredients.indexOfFirst { it.test(pedestal.itemInputCopy) }
					
					if (index != -1){
						usedPedestals.add(pedestal.pos)
						remainingIngredients.removeAt(index)
					}
				}
				
				if (remainingIngredients.isEmpty()){
					return Process(this, usedPedestals.toTypedArray(), recipe)
				}
			}
		}
		
		return null
	}
	
	private class Process : ProcessManyPedestals{
		constructor(table: TileEntityBaseTable, pos: Array<BlockPos>) : super(table, pos)
		constructor(table: TileEntityBaseTable, nbt: TagCompound) : super(table, nbt)
		
		constructor(table: TileEntityBaseTable, ingredientPedestals: Array<BlockPos>, recipe: InfusionRecipe) : this(table, ingredientPedestals){
			this.recipe = recipe
		}
		
		override val energyPerTick =
			Units(1)
		
		override val dustPerTick =
			1 over 4
		
		override val whenFinished =
			CONSUME_ONE
		
		val infusion
			get() = recipe.infusion
		
		private lateinit var recipe: InfusionRecipe
		private var tick = 0
		private var updates = 0
		
		override fun isInputStillValid(oldInput: Array<ItemStack>, newInput: Array<ItemStack>): Boolean{
			return oldInput.indices.all { oldInput[it].item === newInput[it].item } && ::recipe.isInitialized
		}
		
		override fun onWorkTick(context: ITableContext, inputs: Array<ItemStack>): State{
			if (updates >= recipe.updates){
				return runInfusionStep(context)
			}
			
			if (tick < recipe.rate && ++tick < recipe.rate){
				return Work.Success
			}
			
			if (!context.requestUseResources()){
				return Work.Blocked
			}
			
			tick = 0
			++updates
			return Work.Success
		}
		
		private fun runInfusionStep(context: ITableContext): State{
			if (tick > 0 && --tick > 0){
				return Work.Blocked
			}
			
			val infusion = recipe.infusion
			
			val result = context.requestUseSupportingItem {
				if (infusion.tryInfuse(it) != null) it.size.coerceAtMost(4) else 0
			}
			
			if (result != null){
				val infused = infusion.tryInfuse(result.second)
				
				if (infused != null){
					return Output(infused, result.first)
				}
			}
			
			tick = 8
			return Work.Blocked
		}
		
		override fun serializeNBT() = super.serializeNBT().apply {
			putEnum("Recipe", recipe)
			putByte("Tick", tick.toByte())
			putInt("Updates", updates)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = with(nbt){
			super.deserializeNBT(nbt)
			
			getEnum<InfusionRecipe>("Recipe")?.let { recipe = it }
			tick = getByte("Tick").toInt()
			updates = getInt("Updates")
		}
	}
}
