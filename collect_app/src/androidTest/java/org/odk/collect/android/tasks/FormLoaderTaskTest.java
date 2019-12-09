package org.odk.collect.android.tasks;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class FormLoaderTaskTest {
    private static final String SECONDARY_INSTANCE_EXTERNAL_CSV_FORM = "external_csv_form.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FORM = "simple-search-external-csv.xml";
    private static final String SIMPLE_SEARCH_EXTERNAL_CSV_FILE = "simple-search-external-csv-fruits.csv";
    private static final String SIMPLE_SEARCH_EXTERNAL_DB_FILE = "simple-search-external-csv-fruits.db";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(SECONDARY_INSTANCE_EXTERNAL_CSV_FORM,
                    Arrays.asList("external_csv_cities.csv", "external_csv_countries.csv", "external_csv_neighbourhoods.csv")))
            .around(new CopyFormRule(SIMPLE_SEARCH_EXTERNAL_CSV_FORM, Collections.singletonList(SIMPLE_SEARCH_EXTERNAL_CSV_FILE)));

    // Validate the use of CSV files as secondary instances accessed through "jr://file-csv"
    @Test
    public void loadFormWithSecondaryInstanceFromCSV() throws Exception {
        final String formPath = Collect.FORMS_PATH + File.separator + SECONDARY_INSTANCE_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
    }

    // Validate the use of a CSV file externally accessed through search/pulldata
    @Test
    public void loadSearchFromExternalCSV() throws Exception {
        final String formPath = Collect.FORMS_PATH + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
    }

    // Validate the side effects of importing external data for search/pulldata
    @Test
    public void loadSearchFromExternalCSVrenamesFiles() throws Exception {
        final String formPath = Collect.FORMS_PATH + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File importedCSV = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILE);
        Assert.assertFalse("Expected the imported CSV file to have been renamed", importedCSV.exists());

        File renamedCSV = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FILE + ".imported");
        Assert.assertTrue("Expected the renamed CSV file to still be present", renamedCSV.exists());
    }

    // Validate that importing external data multiple times does not fail due to side effects from import
    @Test
    public void loadSearchFromExternalCSVmultipleTimes() throws Exception {
        final String formPath = Collect.FORMS_PATH + File.separator + SIMPLE_SEARCH_EXTERNAL_CSV_FORM;
        // initial load with side effects
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        FormLoaderTask.FECWrapper wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());

        File mediaFolder = wrapper.getController().getMediaFolder();
        File dbFile = new File(mediaFolder + File.separator + SIMPLE_SEARCH_EXTERNAL_DB_FILE);
        Assert.assertTrue(dbFile.exists());
        long dbLastModified = dbFile.lastModified();

        // subsequent load should succeed despite side effects from import
        formLoaderTask = new FormLoaderTask(formPath, null, null);
        wrapper = formLoaderTask.execute(formPath).get();
        Assert.assertNotNull(wrapper);
        Assert.assertNotNull(wrapper.getController());
        Assert.assertEquals("expected file modification timestamp to be unchanged", dbLastModified, dbFile.lastModified());
    }
}
