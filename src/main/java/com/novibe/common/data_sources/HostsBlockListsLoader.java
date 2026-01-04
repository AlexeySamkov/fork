package com.novibe.common.data_sources;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class HostsBlockListsLoader extends ListLoader<String> {

    @Override
    protected Stream<String> lineParser(String urlList) {
        return Pattern.compile("\\r?\\n").splitAsStream(urlList)
                .parallel()
                .map(String::strip)
                .filter(str -> !str.isBlank())
                .filter(line -> !line.startsWith("#"))
                .filter(line -> line.startsWith("0.0.0.0 "))
                .map(line -> line.replace("0.0.0.0 ", ""))
                .map(String::toLowerCase);
    }

    @Override
    protected String listType() {
        return "Block";
    }


}
