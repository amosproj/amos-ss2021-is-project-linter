import { Component } from '@angular/core';
import { FormControl } from '@angular/forms';
import { OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';

import { Config, PagedProjects } from '../schemas';
import { SpinnerComponentComponent } from '../spinner-component/spinner-component.component';
import { ApiService } from '../api.service';
import { StateService } from '../state.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-projects-tab',
  templateUrl: './projects-tab.component.html',
  styleUrls: ['./projects-tab.component.css'],
})
export class ProjectsTabComponent implements OnInit {
  // for search form
  availableSearchCriteria: string[] = [
    'Bestandene Tests',
    'Neue bestandene Tests in den letzten 30 Tagen',
  ];
  chipsControl = new FormControl('');
  searchCriteria = new FormControl(this.availableSearchCriteria[0]);
  // query, sorting parameters
  delta: boolean = false; // whether the bottom radio button is selected
  searchQuery: string = ''; // query from the search bar
  sort: string[]; // selected chips
  // other params
  chipOptions: String[] = [];
  config: Config;
  projects: PagedProjects = <PagedProjects>{ content: [], totalElements: 0 };
  // paging
  pageSizeOptions: number[] = [10, 25, 100];
  currentPage: number = 0;
  pageSize: number = this.pageSizeOptions[0];

  constructor(
    public dialog: MatDialog,
    private api: ApiService,
    private state: StateService,
    private _snackBar: MatSnackBar
  ) {}

  openSnackBar(message: string, action: string) {
    this._snackBar.open(message, action);
  }

  ngOnInit(): void {
    // get config
    this.state.config.subscribe(
      (data) => {
        this.config = data;
        this.chipOptions = this.state.getTags(this.config);
      },
      (error) => {
        console.log(error);
        this.openSnackBar('Fehler beim holen der Config-Datei', 'OK');
      }
    );

    // get projects
    this.getProjects();

    // search query
    this.state.searchQuery.subscribe(
      (query) => {
        this.searchQuery = query;
        this.getProjects();
      },
      (error) => {
        console.log('error');
        this.openSnackBar('Die Suche ist fehlgeschlagen', 'OK');
      }
    );
  }

  ngAfterViewInit() {
    // update sorting query param on change
    this.chipsControl.valueChanges.subscribe((data) => {
      this.sort = data;
    });
    // update sorting query param on change
    this.searchCriteria.valueChanges.subscribe((data) => {
      this.delta = data == this.availableSearchCriteria[1];
    });
  }

  getProjects() {
    // open spinner
    let dialogRef = this.dialog.open(SpinnerComponentComponent, {
      width: '0px',
      height: '0px',
      panelClass: 'custom-dialog-container',
    });

    // make api request
    this.api
      .getAllProjects(
        this.delta,
        this.searchQuery,
        this.sort,
        this.pageSize,
        this.currentPage
      )
      .subscribe(
        (data) => {
          this.projects = data;
        },
        (error) => {
          console.log('error');
          this.openSnackBar('Fehler beim Laden der Projekte', 'OK');
        }
      )
      .add(() => {
        // close spinner
        dialogRef.close();
      });
  }

  updatePagination(pageEvent: PageEvent) {
    this.pageSize = pageEvent.pageSize;
    this.currentPage = pageEvent.pageIndex;
    this.getProjects();
  }
}
