package chylex.hee.game.block
import chylex.hee.game.block.material.Materials
import chylex.hee.init.ModLoot
import chylex.hee.system.util.breakBlock
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemShears
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BlockAncientCobweb : Block(Materials.ANCIENT_COBWEB, MapColor.CLOTH){
	init{
		setHardness(0.2F)
		setLightOpacity(1)
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onBreakSpeed(e: BreakSpeed){
		if (e.state.block != this){
			return
		}
		
		val item = e.entityPlayer.getHeldItem(MAIN_HAND).item
		
		if (item is ItemSword){
			e.newSpeed = e.originalSpeed * 15.8F
		}
		else if (item is ItemShears){
			e.newSpeed = e.originalSpeed * 5.6F
		}
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.ANCIENT_COBWEB.generateDrops(drops, world, fortune)
	}
	
	override fun canSilkHarvest(): Boolean = true
	
	override fun onEntityCollidedWithBlock(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (entity is EntityItem){
			entity.setInWeb()
			entity.motionY = -0.25
		}
		else if (!(entity is EntityPlayer && entity.capabilities.isFlying)){
			pos.breakBlock(world, true)
		}
	}
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? = NULL_AABB
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState): Boolean = false
	override fun isOpaqueCube(state: IBlockState): Boolean = false
	override fun getBlockLayer(): BlockRenderLayer = CUTOUT
}
