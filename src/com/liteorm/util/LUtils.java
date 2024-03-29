package com.liteorm.util;

import java.util.HashMap;
import java.util.Random;

public class LUtils {
	private static HashMap<Integer, String> whats = new HashMap<Integer, String>();
	private static final Random RANDOM = new Random();

    
	public static String randomAlphabetic(int count) {
        return random(count, true, false);
    }
    
    public static String randomAlphanumeric(int count) {
        return random(count, true, true);
    }
    
    public static String randomNumeric(int count) {
        return random(count, false, true);
    }

    public static String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }
    
    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, null, RANDOM);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers, char[] chars) {
        return random(count, start, end, letters, numbers, chars, RANDOM);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers,
                                char[] chars, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if ((start == 0) && (end == 0)) {
            end = 'z' + 1;
            start = ' ';
            if (!letters && !numbers) {
                start = 0;
                end = Integer.MAX_VALUE;
            }
        }

        StringBuffer buffer = new StringBuffer();
        int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (random.nextInt(gap) + start);
            } else {
                ch = chars[random.nextInt(gap) + start];
            }
            if ((letters && numbers && Character.isLetterOrDigit(ch))
                || (letters && Character.isLetter(ch))
                || (numbers && Character.isDigit(ch))
                || (!letters && !numbers)) {
                buffer.append(ch);
            } else {
                count++;
            }
        }
        return buffer.toString();
    }
	
	public static String getWhats(int n){
		String res = whats.get(n);
		if(res==null){
			StringBuilder b = new StringBuilder("(");
			for(int i =0;i<n;i++){
				if(i>0){
					b.append(',');
				}
				b.append('?');
			}
			b.append(')');
			res = b.toString();
			whats.put(n, res);
		}
		return res;
	}
}
