package noobchain;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class BlockList {
    private static ArrayList<Block> blockChain = new ArrayList<>();

    public BlockList(){
    }

    public String blockListToJson(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.blockChain);
    }

    public void addBlockToBlockChain(Block block){
        blockChain.add(block);
    }

    public Block getBlock(int index){
        return blockChain.get(index);
    }

    public int getSize(){
        return blockChain.size();
    }

}
