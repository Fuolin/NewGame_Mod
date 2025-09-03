package com.newgame.handler;

import com.newgame.NewGame;

import java.nio.file.Path;

public class Safe {
    public static Boolean safe(Path path){
        if(test(path))return false;//继续执行
        //越权
        NewGame.LOGGER.error("Security alert: Attempted to access outside root directory: {}", path);
        return true;//不继续执行
    }
    private static Boolean test(Path path){
        if(!path.startsWith(NewGame.getRootPath()))return false;//不满足直接返回
        if(path.startsWith(NewGame.getNewGamePath()))return false;
        if(path.startsWith(NewGame.getModsPath()))return false;
        return true;//都满足
    }
}
