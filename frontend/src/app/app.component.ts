import { Component , ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';
import {routes} from './app-routing.module';
import {provideHttpClient} from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frontend';
}
export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient()]
};