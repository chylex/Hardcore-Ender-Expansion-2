package chylex.hee.game.entity.item
import chylex.hee.system.util.Pos
import chylex.hee.system.util.add
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getHardness
import chylex.hee.system.util.getState
import chylex.hee.system.util.getTile
import chylex.hee.system.util.posVec
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setState
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.Block.NULL_AABB
import net.minecraft.block.BlockFalling
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.MoverType.SELF
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

open class EntityFallingBlockHeavy : EntityFallingBlock, IEntityAdditionalSpawnData{
	companion object{
		private val ignoredTileKeys = setOf("x", "y", "z")
		
		fun canFallThrough(world: World, pos: BlockPos): Boolean{
			val state = pos.getState(world)
			return BlockFalling.canFallThrough(state) || state.block.isReplaceable(world, pos) || (state.getBlockHardness(world, pos) == 0F && state.getCollisionBoundingBox(world, pos) == NULL_AABB)
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, pos: BlockPos, state: IBlockState) : super(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, state)
	
	override fun writeSpawnData(buffer: ByteBuf){
		buffer.writeInt(block?.let(Block::getStateId) ?: 0)
	}
	
	override fun readSpawnData(buffer: ByteBuf){
		fallTile = Block.getStateById(buffer.readInt())
	}
	
	override fun onUpdate(){
		val state = this.block
		
		if (state == null || state.material === Material.AIR){
			if (!world.isRemote){
				setDead()
			}
			
			return
		}
		
		prevPosX = posX
		prevPosY = posY
		prevPosZ = posZ
		
		if (fallTime++ == 0){
			val pos = Pos(this)
			
			if (pos.getBlock(world) === state.block){
				pos.setAir(world)
			}
			else if (!world.isRemote){
				setDead()
				return
			}
		}
		
		if (!hasNoGravity()){
			motionY -= 0.04
		}
		
		move(SELF, motionX, motionY, motionZ)
		
		if (!world.isRemote){
			val pos = Pos(this)
			
			if (onGround){
				val posSlightlyBelow = Pos(posVec.add(0.0, -0.01, 0.0))
				
				if (canFallThrough(world, posSlightlyBelow) && posY != prevPosY){ // if posY hasn't changed, it's stuck on a solid replaceable block
					onGround = false
					return
				}
				
				motionX *= 0.7
				motionZ *= 0.7
				motionY *= -0.5
				
				val collidingWith = pos.getState(world)
				
				if (collidingWith.block !== Blocks.PISTON_EXTENSION){
					val landedOnPos = if (canFallThrough(world, pos)) pos else pos.up() // allow landing on non-full solid blocks, even if it's kinda ugly
					
					if (!placeAfterLanding(landedOnPos, collidingWith)){
						dropBlockIfPossible()
					}
					
					setDead()
				}
			}
			else if ((fallTime > 100 && (pos.y < 1 || pos.y > 256)) || fallTime > 600){
				dropBlockIfPossible()
				setDead()
			}
		}
		
		motionX *= 0.98
		motionY *= 0.98
		motionZ *= 0.98
	}
	
	protected open fun placeAfterLanding(pos: BlockPos, collidingWith: IBlockState): Boolean{
		if (!canFallThrough(world, pos.down()) && canFallThrough(world, pos)){
			val state = block ?: return false
			val block = state.block
			
			if (pos.getHardness(world) == 0F){
				pos.breakBlock(world, true)
			}
			
			if (block.canPlaceBlockAt(world, pos) && pos.setState(world, state)){
				if (block is BlockFalling){
					block.onEndFalling(world, pos, state, collidingWith)
				}
				
				if (tileEntityData != null && block.hasTileEntity(state)){
					pos.getTile<TileEntity>(world)?.let {
						val nbt = it.writeToNBT(NBTTagCompound())
						
						for(key in tileEntityData.keySet){
							if (key !in ignoredTileKeys){
								nbt.setTag(key, tileEntityData.getTag(key).copy())
							}
						}
						
						it.readFromNBT(nbt)
						it.markDirty()
					}
				}
				
				return true
			}
		}
		
		return false
	}
	
	protected open fun dropBlockIfPossible(){
		val state = block
		
		if (!world.isRemote && shouldDropItem && world.gameRules.getBoolean("doEntityDrops") && state != null){
			val block = state.block
			entityDropItem(ItemStack(block, 1, block.damageDropped(state)), 0F)
		}
	}
}
