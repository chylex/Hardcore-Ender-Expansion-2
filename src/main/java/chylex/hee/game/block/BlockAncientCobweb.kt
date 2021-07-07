package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.world.breakBlock
import chylex.hee.system.forge.SubscribeEvent
import chylex.hee.system.math.Vec3
import chylex.hee.system.migration.BlockWeb
import chylex.hee.system.migration.EntityItem
import chylex.hee.system.migration.EntityLivingBase
import chylex.hee.system.migration.EntityMob
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.ItemShears
import chylex.hee.system.migration.ItemSword
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import java.util.Random

class BlockAncientCobweb(builder: BlockBuilder) : BlockWeb(builder.p), IBlockLayerCutout {
	init {
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	@SubscribeEvent
	fun onBreakSpeed(e: BreakSpeed) {
		if (e.state.block !== this) {
			return
		}
		
		val item = e.player.getHeldItem(MAIN_HAND).item
		
		if (item is ItemSword) {
			e.newSpeed = e.originalSpeed * 15.8F
		}
		else if (item is ItemShears) {
			e.newSpeed = e.originalSpeed * 5.6F
		}
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		pos.breakBlock(world, true)
	}
	
	override fun getFlammability(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 300
	}
	
	override fun getFireSpreadSpeed(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction): Int {
		return 100
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		if (entity is EntityItem) {
			entity.setMotionMultiplier(state, Vec3.xyz(0.6))
		}
		else if (!world.isRemote) {
			val canBreak = when(entity) {
				is EntityPlayer     -> !entity.abilities.isFlying
				is EntityMob        -> entity.attackTarget != null && (entity.width * entity.height) > 0.5F
				is EntityLivingBase -> false
				else                -> true
			}
			
			if (canBreak) {
				world.pendingBlockTicks.scheduleTick(pos, this, 1) // delay required to avoid client-side particle crash
			}
		}
	}
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return VoxelShapes.empty()
	}
}
