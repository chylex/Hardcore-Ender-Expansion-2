package chylex.hee.game.block
import chylex.hee.game.block.info.Materials
import chylex.hee.init.ModLoot
import chylex.hee.system.util.breakBlock
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemShears
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand.MAIN_HAND
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Random

class BlockAncientCobweb : Block(Materials.ANCIENT_COBWEB, MapColor.CLOTH){
	init{
		setHardness(0.2F)
		setLightOpacity(1)
		
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onBreakSpeed(e: BreakSpeed){
		if (e.state.block !== this){
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
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		pos.breakBlock(world, true)
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.ANCIENT_COBWEB.generateDrops(drops, world, fortune)
	}
	
	override fun canSilkHarvest() = true
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (entity is EntityItem){
			entity.setInWeb()
			entity.motionY = -0.25
		}
		else if (!world.isRemote){
			val canBreak = when(entity){
				is EntityPlayer     -> !entity.capabilities.isFlying
				is EntityMob        -> entity.attackTarget != null && (entity.width * entity.height) > 0.5F
				is EntityLivingBase -> false
				else                -> true
			}
			
			if (canBreak){
				world.scheduleUpdate(pos, this, 1) // delay required to avoid client-side particle crash
			}
		}
	}
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos) = NULL_AABB
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) = UNDEFINED
	
	override fun isFullCube(state: IBlockState) = false
	override fun isOpaqueCube(state: IBlockState) = false
	override fun getRenderLayer() = CUTOUT
}
