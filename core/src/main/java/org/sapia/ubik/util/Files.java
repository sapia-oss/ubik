package org.sapia.ubik.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public final class Files {

  /**
   * This interface must be implemented by class that interact with the file
   * system traversal method that the {@link Files} class provides.
   * 
   * @author yduchesne
   * 
   */
  public interface FileVisitor {

    /**
     * @param visited
     *          the {@link File} being visited.
     * @return <code>true</code> if traversal should continue,
     *         <code>false</code> if it should stop.
     */
    public boolean visit(File visited);
  }

  // --------------------------------------------------------------------------

  /**
   * A {@link FileVisitor} implemented on top of a {@link FileFilter}: the files
   * that are visited are added to the internal list of files that an instance
   * of this class internally keeps, provided they are accepted by the
   * {@link FileFilter} that the instance of this class wraps.
   * 
   * @see FileFilter
   * 
   * @author yduchesne
   * 
   */
  public static class FileFilterVisitor implements FileVisitor {

    private List<File> result = new ArrayList<File>();
    private FileFilter filter;

    /**
     * @param filter
     *          the {@link FileFilter} that this instance should use.
     */
    public FileFilterVisitor(FileFilter filter) {
      this.filter = filter;
    }

    /**
     * Adds the given file to this instance's internally kept list if it is
     * accepted by this instance's {@link FileFilter}.
     */
    @Override
    public boolean visit(File visited) {
      if (filter.accept(visited)) {
        result.add(visited);
      }
      return true;
    }

    /**
     * @return the {@link List} of {@link File}s that have been accumulated by
     *         this instance.
     */
    public List<File> getResult() {
      return result;
    }

  }

  // --------------------------------------------------------------------------

  /**
   * A {@link FileVisitor} that deletes the files it visits: should be used in
   * conjunction with {@link Files#visitDepthFirst(File, FileVisitor)}.
   */
  public static class FileDeletionVisitor implements FileVisitor {

    private List<File> deletedDirectories = new ArrayList<File>();
    private List<File> deletedFiles = new ArrayList<File>();

    @Override
    public boolean visit(File visited) {
      if (visited.isDirectory()) {
        deletedDirectories.add(visited);
      } else {
        deletedFiles.add(visited);
      }
      visited.delete();
      return true;
    }
  }

  // ==========================================================================

  private Files() {
  }

  /**
   * Visits a file hierarchy, starting from the given root directory. The
   * algorithm implements depth-first traversal.
   * 
   * @param rootDir
   *          a {@link File} corresponding to the root directory of the
   *          hierarchy to traverse.
   * @param visitor
   *          a {@link FileVisitor}
   */
  public static void visitDepthFirst(File rootDir, FileVisitor visitor) {
    if (rootDir.isDirectory()) {
      File[] files = rootDir.listFiles();
      if (files != null) {
        for (File visited : files) {
          visitDepthFirst(visited, visitor);
        }
      }
    }
    visitor.visit(rootDir);
  }

  /**
   * Creates a new {@link FileFilterVisitor} and returns it.
   * 
   * @param filter
   *          the {@link FileFilter} with which the {@link FileFilterVisitor}
   *          should be constructed.
   * @return a new {@link FileFilterVisitor}.
   */
  public static FileFilterVisitor createFileFilterVisitor(FileFilter filter) {
    return new FileFilterVisitor(filter);
  }

  /**
   * @return a new {@link FileDeletionVisitor}.
   */
  public static FileDeletionVisitor createFileDeletionVisitor() {
    return new FileDeletionVisitor();
  }
}
