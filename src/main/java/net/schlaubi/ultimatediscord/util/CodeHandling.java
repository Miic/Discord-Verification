package net.schlaubi.ultimatediscord.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CodeHandling {
	
    private final static Cache<String, UUID> stringToUuid = (Cache) CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final static Cache<UUID, String> uuidToString = (Cache) CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(5, TimeUnit.MINUTES).build();
    
	private final static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567980";
	public final static int CODE_LENGTH = 5;

	
	/**
	 * Generate a random code of a given length
	 * @param length
	 * @return
	 */
    public static String generateString(int length){
        StringBuilder random = new StringBuilder();
        Random rnd = new Random();
        while(random.length() < length){
            int index = (int) (rnd.nextFloat() * CHARS.length());
            random.append(CHARS.charAt(index));
        }
        return random.toString();
    }
    
    /**
     * Generates a code then caches it for the player
     * 
     * @param uuid
     * @param length
     * @return
     */
    public static String generateCodeForPlayer(UUID uuid, int length) {
    	final String code = generateString(length);
        stringToUuid.put(code, uuid);
        uuidToString.put(uuid, code);
        return code;
    }
    
    /**
     * 
     * @param uuid
     * @return
     */
    public static boolean redeemCode(UUID uuid) {
    	final String code = uuidToString.getIfPresent(uuid);
    	if (code != null) {
    		uuidToString.invalidate(uuid);
    		stringToUuid.invalidate(code);
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Utility Getter
     * @return
     */
    public static Cache<String, UUID>  getStringToUUID() {
    	return stringToUuid;
    }
    
    /**
     * 
     */
    public static Cache<UUID, String> getUUIDToString() {
    	return uuidToString;
    }
    
}
