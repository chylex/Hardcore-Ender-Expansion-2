package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_PRISON
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.system.util.Pos
import chylex.hee.system.util.breakBlock
import chylex.hee.system.util.center
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.selectVulnerableEntities
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.EnumDifficulty.PEACEFUL
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Trap_Prison(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	class Trigger : ITriggerHandler{
		private var spawnsLeft = -1
		
		override fun check(world: World): Boolean{
			return !world.isRemote && world.difficulty != PEACEFUL
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world
			val facing = entity.horizontalFacing
			
			val pos1 = Pos(entity)
			val pos2 = pos1.offset(facing, 7).offset(facing.rotateYCCW(), 4).offset(UP, 2)
			
			val (x1, y1, z1) = pos1.center
			val (x2, y2, z2) = pos2.center
			
			val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(AxisAlignedBB(x1, y1, z1, x2, y2, z2)).toList()
			
			if (spawnsLeft == -1){
				if (targets.none()){
					return
				}
				else{
					spawnsLeft = 2 + world.difficulty.id
				}
			}
			
			val rand = world.rand
			val minPos = pos1.min(pos2)
			val maxPos = pos1.max(pos2)
			
			repeat(min(spawnsLeft, rand.nextRounded(1.66F))){
				for(attempt in 1..8){
					val testPos = Pos(rand.nextInt(minPos.x, maxPos.x), maxPos.y + 1, rand.nextInt(minPos.z, maxPos.z))
					
					if (StrongholdPieces.isStoneBrick(testPos.getBlock(world))){
						testPos.breakBlock(world, false)
						
						EntityMobSilverfish(world).apply {
							setLocationAndAngles(testPos.x + 0.5, testPos.y.toDouble(), testPos.z + 0.5, rand.nextFloat(0F, 360F), 0F)
							world.spawnEntity(this)
							
							spawnExplosionParticle()
							attackTarget = rand.nextItemOrNull(targets)
						}
						
						--spawnsLeft
						break
					}
				}
			}
			
			if (spawnsLeft == 0){
				entity.setDead()
			}
		}
		
		override fun nextTimer(rand: Random): Int{
			return 14
		}
		
		override fun serializeNBT() = NBTTagCompound().apply {
			setShort("SpawnsLeft", spawnsLeft.toShort())
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			spawnsLeft = getShort("SpawnsLeft").toInt()
		}
	}
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(maxX, 0, maxZ - 2), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, maxZ - 2), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(1, 1, 5), EntityStructureTrigger(STRONGHOLD_TRAP_PRISON, EAST))
		
		val rand = world.rand
		
		// Chest
		
		world.addTrigger(Pos(1, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// Skull
		
		val skullType = if (rand.nextInt(3) == 0) 2 else 0
		world.addTrigger(Pos(maxX - 1, 1, 1), TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(skullType); skullRotation = 10 }))
		
		// Redstone
		
		repeat(5 + rand.nextInt(5 + rand.nextInt(6))){
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, 5))
			
			if (world.isAir(redstonePos)){
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
