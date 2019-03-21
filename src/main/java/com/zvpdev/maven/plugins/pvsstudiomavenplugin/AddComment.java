package com.zvpdev.maven.plugins.pvsstudiomavenplugin;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "add-comment")
public class AddComment extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "add-comment.commentType", required =  true, readonly = true)
    private CommentType commentType;

    private final static String ACADEMIC_LIC_COMMENT =
            "// This is a personal academic project. Dear PVS-Studio, please check it." +
            System.lineSeparator() +
            "// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com" +
            System.lineSeparator();

    private final static String OPENSOURCE_LIC_COMMENT =
            "// This is an open source non-commercial project. Dear PVS-Studio, please check it." +
            System.lineSeparator() +
            "// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com" +
            System.lineSeparator();

    private final static String PRIVATE_LIC_COMMENT =
            "// This is an independent project of an individual developer. Dear PVS-Studio, please check it." +
            System.lineSeparator() +
            "// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com" +
            System.lineSeparator();

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<File> projectSourceRoots = (List<File>) project.getCompileSourceRoots().stream()
                .map(p -> new File((String) p))
                .collect(Collectors.toList());

        projectSourceRoots.forEach(p -> {
            getLog().info("Project source root: " + p.getAbsolutePath());

            try {
                Files.walkFileTree(p.toPath(), new HashSet<>(), Integer.MAX_VALUE, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        File obj = new File(file.toUri());

                        if (obj.isFile() && FilenameUtils.getExtension(obj.getName()).toLowerCase().equals("java")) {
                            getLog().info("Processing file " + obj.getAbsolutePath());

                            try {
                                byte[] fileBytes = Files.readAllBytes(obj.toPath());

                                if (fileBytes[0] == "/".getBytes()[0]) {
                                    getLog().warn("File " + obj.getAbsolutePath() + " already has a comment");
                                    return FileVisitResult.CONTINUE;
                                }

                                byte[] commentBytes = new byte[]{};
                                switch (commentType) {
                                    case ACADEMIC:
                                        commentBytes = ACADEMIC_LIC_COMMENT.getBytes();
                                        break;
                                    case OPENSOURCE:
                                        commentBytes = OPENSOURCE_LIC_COMMENT.getBytes();
                                        break;
                                    case PRIVATE:
                                        commentBytes = PRIVATE_LIC_COMMENT.getBytes();
                                }

                                int commentBytesLen = Array.getLength(commentBytes);
                                int fileBytesLen = Array.getLength(fileBytes);

                                byte[] resultBytes = (byte[]) Array.newInstance(Byte.TYPE, commentBytesLen + fileBytesLen);
                                System.arraycopy(commentBytes, 0, resultBytes, 0, commentBytesLen);
                                commentBytes = null;
                                System.arraycopy(fileBytes, 0, resultBytes, commentBytesLen, fileBytesLen);
                                fileBytes = null;

                                Files.write(file, resultBytes, StandardOpenOption.WRITE);

                            }catch (IOException e) {
                                getLog().error("Can't add comment to file " + obj.getAbsolutePath(), e);
                            }
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        getLog().error("File visit failed", exc);
                        return FileVisitResult.TERMINATE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });

            } catch (IOException e) {
                getLog().error("Project source root walk file tree error", e);
            }

        });
    }
}
