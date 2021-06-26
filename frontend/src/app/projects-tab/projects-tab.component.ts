import { ViewChild } from '@angular/core';
import { Component } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { environment } from 'src/environments/environment';
import { Project, Config, CheckResults } from '../schemas';
import { RepositoryComponent } from '../repository/repository.component';
import { SpinnerComponentComponent } from '../spinner-component/spinner-component.component';
import { ApiService } from '../api.service';
import { StateService } from '../state.service';

@Component({
  selector: 'app-projects-tab',
  templateUrl: './projects-tab.component.html',
  styleUrls: ['./projects-tab.component.css'],
})
export class ProjectsTabComponent implements OnInit {
  title = 'frontend';
  projectComponents = [];
  chipsControl = new FormControl('');
  chipsValue$ = this.chipsControl.valueChanges;
  kategorie = new FormControl('');
  all_projects: Project[];
  init_all_projects: Project[];
  forwardLinkWorked = true;
  errorMsgForwardLink = '';
  searchCriteria = new FormControl('');
  availableSearchCriteria: string[] = [
    'Bestandene Tests',
    'Neue bestandene Tests in den letzten 30 Tagen',
  ];
  chipOptions: string[];
  filterInfo = 'Momentan sortiert nach Kategorie: - und Sortierkriterium: -';
  toggleToTrue = true;

  config: Config;
  suchBegriff: string;
  projects: Project[];
  searchQuery: string = '';

  constructor(
    public dialog: MatDialog,
    private http: HttpClient,
    private api: ApiService,
    private state: StateService
  ) {}

  ngOnInit(): void {
    // get Config
    this.api.getConfig().subscribe((data) => {
      this.config = data;
    });

    this.getProjects();

    // search query
    this.state.searchQuery.subscribe((query) => {
      this.searchQuery = query;
      console.log(query);
    });
  }


  getProjects() {
    // Holt alle Projekte vom Backend-Server
    let dialogRef = this.dialog.open(SpinnerComponentComponent, {
      width: '0px',
      height: '0px',
      panelClass: 'custom-dialog-container',
    });

    this.api.getAllProjects(false, true, this.searchQuery).subscribe((data) => {
      console.log(data);
      this.projects = data;
    });

    dialogRef.close();
  }

  forwardLink(URL) {
    // Wird aktuell nicht benötigt
    const headers = { 'Content-Type': 'text/html' };

    let HTTPOptions: Object = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      responseType: 'text',
    };

    this.http
      .post<String>(`${environment.baseURL}/projects`, URL, HTTPOptions)
      .subscribe(
        (val: any) => {
          console.log('POST call successful value returned in body', val);
          var regex404 = new RegExp('404 NOT_FOUND', 'i');
          console.log(val.search(regex404));
          if (val.search(regex404) != -1) {
            this.errorMsgForwardLink = 'Fehler 404, bitte URL überprüfen';
            this.forwardLinkWorked = false;
            console.log(this.forwardLinkWorked);
          } else {
            this.forwardLinkWorked = true;
          }
          console.log(this.forwardLinkWorked);
        },
        (error) => {
          console.log('POST call in error', error);
          this.errorMsgForwardLink = 'Internal server error';
          this.forwardLinkWorked = false;
        }
      );
  }

  

  getChipOptions() {
    //hole alle verschiedenen tags aus der config.json datei
    this.chipOptions = [];
    for (let [key, value] of Object.entries(this.config.checks)) {
      if (!this.chipOptions.includes(value.tag)) {
        this.chipOptions.push(value.tag);
      }
    }
  }

  prepareProjectDataForSorting() {
    for (var i = 0; i < this.all_projects.length; i++) {
      var checkResults: CheckResults[] =
        this.all_projects[i].lintingResults[0].checkResults;
      var checkResultsLastMonth: CheckResults[] =
        this.all_projects[i].lintingResults[
          this.all_projects[i].lintingResults.length - 1
        ].checkResults;
      //Zähler für erfolgreiche Checks pro Tag

      var checksPassed: number[] = new Array(this.chipOptions.length).fill(0);
      //Zähler für erfolgreiche Checks letzten Monat pro Tag
      var checksPassedLastMonth: number[] = new Array(
        this.chipOptions.length
      ).fill(0);
      for (var j = 0; j < checkResults.length - 1; j++) {
        // wenn der Check erfolgreich war erhöhe die Zähler
        if (checkResults[j].result) {
          checksPassed[this.chipOptions.indexOf(checkResults[j].tag)] += 1;
        }
        if (checkResultsLastMonth[j].result) {
          checksPassedLastMonth[
            this.chipOptions.indexOf(checkResults[j].tag)
          ] += 1;
        }
      }
      var newChecksPassedLastMonth: number[] = new Array(
        this.chipOptions.length
      );
      for (var j = 0; j < this.chipOptions.length; j++) {
        newChecksPassedLastMonth[j] =
          checksPassed[j] - checksPassedLastMonth[j];
      }
      //var info : GridInfo = {project : val.name, testsPassed: checksPassed};
      this.all_projects[i].passedTestsPerTag = checksPassed;
      this.all_projects[i].newPassedTestsPerTagLastMonth =
        newChecksPassedLastMonth;
      this.all_projects[i].passedTestsInFilter = 0;
      this.all_projects[i].newPassedTestsLastMonth = 0;
    }
  }

  sortProjects() {
    //stelle die initale unsortierte Reihenfolge der Projekte wieder her
    this.all_projects = this.init_all_projects.slice();

    //aktualisiere die Filter Info
    this.filterInfo = 'Momentan sortiert nach Kategorie: ';
    for (var i = 0; i < this.chipsControl.value.length; i++) {
      this.filterInfo += this.chipsControl.value[i];
      if (i != this.chipsControl.value.length - 1) {
        this.filterInfo += ', ';
      }
    }
    this.filterInfo += ' und Sortierkriterium: ' + this.searchCriteria.value;

    //setze die notwendigen variablen fürs sortieren der projekte nach den gewählten tags
    for (let project in this.all_projects) {
      this.all_projects[project].passedTestsInFilter = 0;
      this.all_projects[project].newPassedTestsLastMonth = 0;
      for (var i = 0; i < this.chipOptions.length; i++) {
        for (var j = 0; j < this.chipsControl.value.length; j++) {
          if (this.chipsControl.value[j] == this.chipOptions[i]) {
            this.all_projects[project].passedTestsInFilter +=
              this.all_projects[project].passedTestsPerTag[i];
            this.all_projects[project].newPassedTestsLastMonth +=
              this.all_projects[project].newPassedTestsPerTagLastMonth[i];
          }
        }
      }
    }

    //wähle die sortier funktion nach eingabe
    console.log('Projekte vor sortieren!', this.all_projects);
    switch (this.searchCriteria.value) {
      case 'Bestandene Tests': {
        this.all_projects.sort(this.compareTestsPassedPerActivFilter);
        break;
      }
      case 'Neue bestandene Tests in den letzten 30 Tagen': {
        this.all_projects.sort(this.compareNewTestsPassedSinceLastMonthFilter);
        break;
      }
    }
    console.log('Projekte nach sortieren', this.all_projects);
  }

  compareNewTestsPassedSinceLastMonthFilter(a, b) {
    if (a.newPassedTestsLastMonth < b.newPassedTestsLastMonth) {
      return 1;
    }
    if (a.newPassedTestsLastMonth > b.newPassedTestsLastMonth) {
      return -1;
    }
    return 0;
  }

  compareTestsPassedPerActivFilter(a, b) {
    if (a.passedTestsInFilter < b.passedTestsInFilter) {
      return 1;
    }
    if (a.passedTestsInFilter > b.passedTestsInFilter) {
      return -1;
    }
    return 0;
  }
}
