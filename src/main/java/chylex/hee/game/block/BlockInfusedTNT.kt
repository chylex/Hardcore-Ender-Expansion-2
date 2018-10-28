package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityInfusedTNT
import chylex.hee.game.entity.item.EntityInfusedTNT
import chylex.hee.game.item.infusion.Infusion.TRAP
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.system.util.getTile
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setAir
import net.minecraft.block.BlockTNT
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

class BlockInfusedTNT : BlockTNT(), ITileEntityProvider{
	init{
		soundType = SoundType.PLANT
		setCreativeTab(null)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityInfusedTNT()
	}
	
	// Placement & drops
	
	override fun onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack){
		pos.getTile<TileEntityInfusedTNT>(world)?.infusions = InfusionTag.getList(stack)
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null){
			drops.add(ItemStack(this).also { InfusionTag.setList(it, infusions) })
		}
	}
	
	// Delay block addition/deletion
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		world.scheduleUpdate(pos, this, 1)
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.onBlockAdded(world, pos, state)
	}
	
	override fun removedByPlayer(state: IBlockState, world: World, pos: BlockPos, player: EntityPlayer, willHarvest: Boolean): Boolean{
		if (pos.getTile<TileEntityInfusedTNT>(world)?.infusions?.has(TRAP) == true && !player.isCreative){
			explode(world, pos, state.withProperty(EXPLODE, true), player)
			pos.setAir(world)
			return false
		}
		
		if (willHarvest){
			return true // skip super call before drops
		}
		
		return super.removedByPlayer(state, world, pos, player, willHarvest)
	}
	
	override fun harvestBlock(world: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, tile: TileEntity?, stack: ItemStack){
		super.harvestBlock(world, player, pos, state, tile, stack)
		pos.setAir(world)
	}
	
	// TNT overrides
	
	override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion){
		if (world.isRemote){
			return
		}
		
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null){
			world.spawnEntity(EntityInfusedTNT(world, pos, infusions, explosion))
		}
	}
	
	override fun explode(world: World, pos: BlockPos, state: IBlockState, igniter: EntityLivingBase?){
		if (world.isRemote){
			return
		}
		
		val infusions = pos.getTile<TileEntityInfusedTNT>(world)?.infusions
		
		if (infusions != null && state.getValue(EXPLODE)){
			if (infusions.has(TRAP) && world.isBlockPowered(pos)){
				dropBlockAsItem(world, pos, state, 0)
			}
			else{
				EntityInfusedTNT(world, pos, infusions, igniter).apply {
					SoundEvents.ENTITY_TNT_PRIMED.playServer(world, posVec, SoundCategory.BLOCKS)
					world.spawnEntity(this)
				}
			}
		}
	}
}
