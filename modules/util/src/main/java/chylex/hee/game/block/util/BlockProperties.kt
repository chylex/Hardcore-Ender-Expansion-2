package chylex.hee.game.block.util

import net.minecraft.block.AbstractButtonBlock
import net.minecraft.block.BedBlock
import net.minecraft.block.BrewingStandBlock
import net.minecraft.block.CauldronBlock
import net.minecraft.block.ChestBlock
import net.minecraft.block.ChorusFlowerBlock
import net.minecraft.block.DispenserBlock
import net.minecraft.block.DoorBlock
import net.minecraft.block.FlowingFluidBlock
import net.minecraft.block.FourWayBlock
import net.minecraft.block.FurnaceBlock
import net.minecraft.block.LeavesBlock
import net.minecraft.block.RotatedPillarBlock
import net.minecraft.block.SkullBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.TrapDoorBlock
import net.minecraft.block.VineBlock
import net.minecraft.block.WallSkullBlock
import net.minecraft.state.BooleanProperty
import net.minecraft.state.DirectionProperty
import net.minecraft.state.EnumProperty
import net.minecraft.state.IntegerProperty
import net.minecraft.state.properties.AttachFace
import net.minecraft.state.properties.BedPart
import net.minecraft.state.properties.ChestType
import net.minecraft.state.properties.DoubleBlockHalf
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.SlabType
import net.minecraft.state.properties.StairsShape
import net.minecraft.util.Direction.Axis

val BED_OCCUPIED: BooleanProperty
	get() = BedBlock.OCCUPIED

val BED_PART: EnumProperty<BedPart>
	get() = BedBlock.PART

val BREWING_STAND_HAS_BOTTLE: Array<BooleanProperty>
	get() = BrewingStandBlock.HAS_BOTTLE

val BUTTON_ATTACH_FACE: EnumProperty<AttachFace>
	get() = AbstractButtonBlock.FACE

val CAULDRON_LEVEL: IntegerProperty
	get() = CauldronBlock.LEVEL

val CHEST_FACING: DirectionProperty
	get() = ChestBlock.FACING

val CHEST_TYPE: EnumProperty<ChestType>
	get() = ChestBlock.TYPE

val CHORUS_FLOWER_AGE: IntegerProperty
	get() = ChorusFlowerBlock.AGE

val DISPENSER_FACING: DirectionProperty
	get() = DispenserBlock.FACING

val DOOR_HALF: EnumProperty<DoubleBlockHalf>
	get() = DoorBlock.HALF

val FLUID_LEVEL: IntegerProperty
	get() = FlowingFluidBlock.LEVEL

val FOUR_WAY_EAST: BooleanProperty
	get() = FourWayBlock.EAST

val FOUR_WAY_NORTH: BooleanProperty
	get() = FourWayBlock.NORTH

val FOUR_WAY_SOUTH: BooleanProperty
	get() = FourWayBlock.SOUTH

val FOUR_WAY_WEST: BooleanProperty
	get() = FourWayBlock.WEST

val FURNACE_FACING: DirectionProperty
	get() = FurnaceBlock.FACING

val FURNACE_LIT: BooleanProperty
	get() = FurnaceBlock.LIT

val LEAVES_DISTANCE: IntegerProperty
	get() = LeavesBlock.DISTANCE

val LEAVES_PERSISTENT: BooleanProperty
	get() = LeavesBlock.PERSISTENT

val ROTATED_PILLAR_AXIS: EnumProperty<Axis>
	get() = RotatedPillarBlock.AXIS

val SKULL_ROTATION: IntegerProperty
	get() = SkullBlock.ROTATION

val SKULL_WALL_FACING: DirectionProperty
	get() = WallSkullBlock.FACING

val SLAB_TYPE: EnumProperty<SlabType>
	get() = SlabBlock.TYPE

val STAIRS_FACING: DirectionProperty
	get() = StairsBlock.FACING

val STAIRS_HALF: EnumProperty<Half>
	get() = StairsBlock.HALF

val STAIRS_SHAPE: EnumProperty<StairsShape>
	get() = StairsBlock.SHAPE

val TRAPDOOR_HALF: EnumProperty<Half>
	get() = TrapDoorBlock.HALF

val TRAPDOOR_OPEN: BooleanProperty
	get() = TrapDoorBlock.OPEN

val VINE_UP: BooleanProperty
	get() = VineBlock.UP
