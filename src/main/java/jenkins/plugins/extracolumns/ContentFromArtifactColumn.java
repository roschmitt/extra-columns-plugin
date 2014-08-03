/*
 * The MIT License
 *
 * Copyright (c) 2012, Frederic Gurr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jenkins.plugins.extracolumns;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumnDescriptor;
import hudson.views.ListViewColumn;
import hudson.model.Run;
import hudson.model.Run.Artifact;
import java.util.List;

import jenkins.model.ArtifactManager;
import jenkins.util.VirtualFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentFromArtifactColumn extends ListViewColumn {

    private static final Logger LOGGER = Logger.getLogger(SlaveOrLabelColumn.class.getName());

    private int columnWidth;
    private boolean forceWidth;
    private String artifactFileName;

    
    @DataBoundConstructor
    public ContentFromArtifactColumn(String artifactFileName, int columnWidth, boolean forceWidth) {
        super();
        this.artifactFileName = artifactFileName;
        this.columnWidth = columnWidth;
        this.forceWidth = forceWidth;
    }

    public ContentFromArtifactColumn() {
        this("", 80, false);
    }

    public String getArtifactFileName() {
      return artifactFileName;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public boolean isForceWidth() {
        return forceWidth;
    }

    public String getArtifactContent(@SuppressWarnings("rawtypes") Job job) throws IOException {
        if (job == null || job.getLastCompletedBuild().getHasArtifacts() == false) {
          return "unavail";
        }
        String str = "unavail";
        Run run = job.getLastCompletedBuild();
        List <Run.Artifact> artifacts = job.getLastCompletedBuild().getArtifacts();
        for (Run.Artifact artifact : artifacts) {
          LOGGER.info("Actual artifact: "+artifact.getFileName()+", requested artifact: "+artifactFileName);
          if (artifactFileName.equals(artifact.getFileName())) {
            ArtifactManager am = artifact.getArtifactManager();
            VirtualFile vf = am.root();
            if (!vf.isFile()) {
              return "not a file: "+artifact.getFileName();
            }
            if (!vf.canRead()) {
              return "cannot read: "+artifact.getFileName();
            }
            LOGGER.info("vf: "+vf.child(artifact.relativePath));
            return artifact.relativePath; //TODO: read the file, readFile(artifact.getartifact.getFileName());
          }
        }
        return str;
    }

    public String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            String line = br.readLine();
            LOGGER.info("Read: "+line);
            if (line != null) {
                return line.trim();
            }
            LOGGER.warning("The first line of the artifact file "+fileName+" was null.");
            return "First line null in "+fileName;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Failed to read artifact file "+fileName,e);
            throw e;
        } finally {
            br.close();
        }
    }

    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.ContentFromArtifactColumn_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/extra-columns/help-buildDescription-column.html";
        }

    }
}
