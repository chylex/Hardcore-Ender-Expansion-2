package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.init.ModItems
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getTile
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.setAir
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.IProjectile
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random
import kotlin.math.pow

class BlockEnergyCluster(builder: BlockSimple.Builder) : BlockSimple(builder), ITileEntityProvider{
	private companion object{
		val SELECTION_AABB = AxisAlignedBB(0.35, 0.35, 0.35, 0.65, 0.65, 0.65)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityEnergyCluster()
	}
	
	override fun onBlockHarvested(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer){
		if (player.capabilities.isCreativeMode && !player.isSneaking){
			pos.getTile<TileEntityEnergyCluster>(world)?.breakWithoutExplosion = true
		}
	}
	
	override fun breakBlock(world: World, pos: BlockPos, state: IBlockState){
		val tile = pos.getTile<TileEntityEnergyCluster>(world) ?: return
		super.breakBlock(world, pos, state) // removes the tile entity
		
		if (tile.breakWithoutExplosion){
			return
		}
		
		val units = tile.energyLevel.units.value.toFloat()
		
		val explosionStength = 2.5F + (units.pow(0.6F) * 0.1F)
		val corruptedEnergyRadius = 2F + (units.pow(0.75F) / 75F)
		val corruptedEnergyLevel = (3F + (units.pow(0.75F) * 0.1F)).ceilToInt().coerceAtMost(20) // TODO replace with a BlockCorruptedEnergy constant
		val ethereumToDrop = (world.rand.nextFloat(1.6F, 2.0F) * (units * 0.01F).pow(1.4F)).floorToInt()
		
		world.newExplosion(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, explosionStength, false, true)
		
		// TODO spawn corrupted energy
		
		repeat(ethereumToDrop){
			spawnAsEntity(world, pos, ItemStack(ModItems.ETHEREUM))
		}
	}
	
	override fun onEntityCollidedWithBlock(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (entity is IProjectile){
			pos.setAir(world)
		}
	}
	
	override fun quantityDropped(rand: Random): Int = 0
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? = NULL_AABB
	override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB = SELECTION_AABB.offset(pos)
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	override fun getRenderType(state: IBlockState): EnumBlockRenderType = INVISIBLE
}
