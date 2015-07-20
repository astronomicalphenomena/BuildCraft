/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.util.Constants;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.IterableAlgorithmRunner;
import buildcraft.core.lib.utils.PathFinding;

public class AIRobotGotoBlock extends AIRobotGoto {

    private PathFinding pathSearch;
    private IterableAlgorithmRunner pathSearchJob;
    private LinkedList<BlockPos> path;
    private double prevDistance = Double.MAX_VALUE;
    private float finalX, finalY, finalZ;
    private double maxDistance = 0;
    private BlockPos lastBlockInPath;

    public AIRobotGotoBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos) {
        this(robot);
        finalX = x;
        finalY = y;
        finalZ = z;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, BlockPos pos, double iMaxDistance) {
        this(robot, pos);

        maxDistance = iMaxDistance;
    }

    public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> iPath) {
        this(robot);
        path = iPath;
        finalX = path.getLast().x;
        finalY = path.getLast().y;
        finalZ = path.getLast().z;
        setNextInPath();
    }

    @Override
    public void start() {
        robot.undock();
    }

    @Override
    public void update() {
        if (path == null && pathSearch == null) {
            pathSearch = new PathFinding(robot.worldObj, new BlockPos((int) Math.floor(robot.posX), (int) Math.floor(robot.posY), (int) Math.floor(
                    robot.posZ)), new BlockPos((int) Math.floor(finalX), (int) Math.floor(finalY), (int) Math.floor(finalZ)), maxDistance);

            pathSearchJob = new IterableAlgorithmRunner(pathSearch, 100);
            pathSearchJob.start();
        } else if (path != null) {
            double distance = robot.getDistance(nextX, nextY, nextZ);

            if (!robot.isMoving() || distance > prevDistance) {
                if (path.size() > 0) {
                    path.removeFirst();
                }

                setNextInPath();
            } else {
                prevDistance = robot.getDistance(nextX, nextY, nextZ);
            }
        } else {
            if (pathSearchJob.isDone()) {
                path = pathSearch.getResult();

                if (path.size() == 0) {
                    setSuccess(false);
                    terminate();
                    return;
                }

                lastBlockInPath = path.getLast();

                setNextInPath();
            }
        }

        if (path != null && path.size() == 0) {
            robot.motionX = 0;
            robot.motionY = 0;
            robot.motionZ = 0;

            if (lastBlockInPath != null) {
                robot.posX = lastBlockInPath.x + 0.5F;
                robot.posY = lastBlockInPath.y + 0.5F;
                robot.posZ = lastBlockInPath.z + 0.5F;
            }
            terminate();
        }
    }

    private void setNextInPath() {
        if (path.size() > 0) {
            BlockPos next = path.getFirst();
            setDestination(robot, next.x + 0.5F, next.y + 0.5F, next.z + 0.5F);
            prevDistance = Double.MAX_VALUE;
            robot.aimItemAt(next.x, next.y, next.z);
        }
    }

    @Override
    public void end() {
        if (pathSearchJob != null) {
            pathSearchJob.terminate();
            robot.motionX = 0;
            robot.motionY = 0;
            robot.motionZ = 0;
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        nbt.setFloat("finalX", finalX);
        nbt.setFloat("finalY", finalY);
        nbt.setFloat("finalZ", finalZ);
        nbt.setDouble("maxDistance", maxDistance);

        if (path != null) {
            NBTTagList pathList = new NBTTagList();

            for (BlockPos i : path) {
                NBTTagCompound subNBT = new NBTTagCompound();
                i.writeTo(subNBT);
                pathList.appendTag(subNBT);
            }

            nbt.setTag("path", pathList);
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        super.loadSelfFromNBT(nbt);

        finalX = nbt.getFloat("finalX");
        finalY = nbt.getFloat("finalY");
        finalZ = nbt.getFloat("finalZ");
        maxDistance = nbt.getDouble("maxDistance");

        if (nbt.hasKey("path")) {
            NBTTagList pathList = nbt.getTagList("path", Constants.NBT.TAG_COMPOUND);

            path = new LinkedList<BlockPos>();

            for (int i = 0; i < pathList.tagCount(); ++i) {
                path.add(new BlockPos(pathList.getCompoundTagAt(i)));
            }

            setNextInPath();
        }
    }
}
