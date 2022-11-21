package it.gov.innovazione.lodviewng.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceClassPathLoader {

    @SneakyThrows
    public static File toFile(@NonNull Resource resource) {
        String fileExtension = Optional.of(resource)
                .map(Resource::getFilename)
                .map(s -> s.substring(s.lastIndexOf(".")))
                .orElse("");
        InputStream initialStream = resource.getInputStream();
        File tempFileName = File.createTempFile("lodview", fileExtension);
        FileUtils.copyInputStreamToFile(initialStream, tempFileName);
        return tempFileName;
    }

    public static File toFile(String resource) {
        return toFile(new ClassPathResource(resource));
    }

    @SneakyThrows
    public static List<File> toFiles(String directory) {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:" + directory + "/*");
        return Arrays.stream(resources)
                .map(ResourceClassPathLoader::toFile)
                .collect(Collectors.toList());
    }
}
