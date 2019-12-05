package org.odk.collect.android.forms;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FileReferenceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FormUtils {

    private FormUtils() {
        
    }

    public static void setupReferenceManagerForForm(ReferenceManager referenceManager, File formMediaDir) {

        // Remove previous forms
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // this is /sdcard/odk
            referenceManager.addReferenceFactory(new FileReferenceFactory(Collect.ODK_ROOT));
        }

        addSessionRootTranslators(referenceManager,
                buildSessionRootTranslators(formMediaDir.getName(), enumerateHostStrings()));
    }

    protected static String[] enumerateHostStrings() {
        return new String[] {"images", "image", "audio", "video", "file-csv", "file"};
    }

    protected List<RootTranslator> buildSessionRootTranslators(String formMediaDir, String[] hostStrings) {
        List<RootTranslator> rootTranslators = new ArrayList<>();
        // Set jr://... to point to /sdcard/odk/forms/formBasename-media/
        final String translatedPrefix = String.format("jr://file/forms/" + formMediaDir + "/");
        for (String t : hostStrings) {
            rootTranslators.add(new RootTranslator(String.format("jr://%s/", t), translatedPrefix));
        }
        return rootTranslators;
    }

    private void addSessionRootTranslators(ReferenceManager referenceManager, List<RootTranslator> rootTranslators) {
        for (RootTranslator rootTranslator : rootTranslators) {
            referenceManager.addSessionRootTranslator(rootTranslator);
        }
    }
}
