/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import buildcraft.api.core.BCLog;

import io.netty.buffer.ByteBuf;

public class PacketNBT extends PacketCoordinates {

    private NBTTagCompound nbttagcompound;

    public PacketNBT() {}

    public PacketNBT(int id, NBTTagCompound nbttagcompound, BlockPos pos) {
        super(id, pos);
        this.nbttagcompound = nbttagcompound;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(nbttagcompound, baos);
            byte[] compressed = baos.toByteArray();
            if (compressed.length > 65535) {
                BCLog.logger.error("NBT data is too large (" + compressed.length + " > 65535)! Please report!");
            }
            data.writeShort(compressed.length);
            data.writeBytes(compressed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        int length = data.readUnsignedShort();
        byte[] compressed = new byte[length];
        data.readBytes(compressed);

        try {
            this.nbttagcompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(compressed));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NBTTagCompound getTagCompound() {
        return this.nbttagcompound;
    }

}
