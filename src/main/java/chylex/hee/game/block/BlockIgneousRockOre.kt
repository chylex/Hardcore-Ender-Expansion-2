package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.FIRE_TYPE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.particle.spawner.ParticleSpawnerVanilla
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModLoot
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.EnumParticleTypes.LAVA
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.BreakEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockIgneousRockOre(builder: BlockBuilder) : BlockSimple(builder){
	private companion object{
		private val DAMAGE_MINING = Damage(FIRE_TYPE(15), PEACEFUL_EXCLUSION)
		
		private val PARTICLE_TICK = ParticleSpawnerVanilla(
			type = LAVA,
			pos = InBox(0.825F)
		)
		
		private fun causeMiningDamage(player: EntityPlayer){
			DAMAGE_MINING.dealTo(2F, player, Damage.TITLE_IN_FIRE)
		}
		
		private fun getToolHarvestLevel(stack: ItemStack): Int?{
			return (stack.item as? ItemTool)?.let {
				try{
					ToolMaterial.valueOf(it.toolMaterialName).harvestLevel
				}catch(e: IllegalArgumentException){
					null
				}
			}
		}
	}
	
	init{
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.IGNEOUS_ROCK_ORE.generateDrops(drops, world, fortune)
	}
	
	override fun getExpDrop(state: IBlockState, world: IBlockAccess, pos: BlockPos, fortune: Int): Int{
		return 9
	}
	
	// Custom harvesting handling
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, tile: TileEntity?, stack: ItemStack){
		val heldItem = player.getHeldItem(MAIN_HAND)
		val tierDifference = getToolHarvestLevel(heldItem)?.let { super.getHarvestLevel(state) - it } ?: return
		
		if (tierDifference > 0){
			heldItem.damageItem(tierDifference, player)
			causeMiningDamage(player)
		}
		else{
			super.harvestBlock(world, player, pos, state, tile, stack)
		}
	}
	
	override fun dropBlockAsItemWithChance(world: World, pos: BlockPos, state: IBlockState, chance: Float, fortune: Int){
		super.dropBlockAsItemWithChance(world, pos, state, chance, fortune)
		harvesters.get()?.let(::causeMiningDamage)
	}
	
	@SubscribeEvent
	fun onBlockBreak(e: BreakEvent){
		if (e.state.block === this){
			val harvestLevel = e.player?.getHeldItem(MAIN_HAND)?.let(::getToolHarvestLevel) ?: 0
			
			if (harvestLevel < super.getHarvestLevel(e.state)){
				e.expToDrop = 0
			}
		}
	}
	
	override fun getHarvestLevel(state: IBlockState) = 0 // use super.getHarvestLevel for the real value
	override fun canHarvestBlock(world: IBlockAccess, pos: BlockPos, player: EntityPlayer) = true
	override fun canSilkHarvest() = true
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	override fun randomDisplayTick(state: IBlockState, world: World, pos: BlockPos, rand: Random){
		if (rand.nextInt(4) != 0){
			PARTICLE_TICK.spawn(Point(pos, 1), rand)
		}
	}
}
