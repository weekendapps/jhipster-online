/**
 * Copyright 2017-2023 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster Online project, see https://github.com/jhipster/jhipster-online
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of } from 'rxjs';

import { JhonlineTestModule } from '../../../test.module';
import { ConfigurationComponent } from 'app/admin/configuration/configuration.component';
import { ConfigurationService, Bean, PropertySource } from 'app/admin/configuration/configuration.service';

describe('Component Tests', () => {
  describe('ConfigurationComponent', () => {
    let comp: ConfigurationComponent;
    let fixture: ComponentFixture<ConfigurationComponent>;
    let service: ConfigurationService;

    beforeEach(
      waitForAsync(() => {
        TestBed.configureTestingModule({
          imports: [JhonlineTestModule],
          declarations: [ConfigurationComponent],
          providers: [ConfigurationService]
        })
          .overrideTemplate(ConfigurationComponent, '')
          .compileComponents();
      })
    );

    beforeEach(() => {
      fixture = TestBed.createComponent(ConfigurationComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ConfigurationService);
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN
        const beans: Bean[] = [
          {
            prefix: 'jhipster',
            properties: {
              clientApp: {
                name: 'jhipsterApp'
              }
            }
          }
        ];
        const propertySources: PropertySource[] = [
          {
            name: 'server.ports',
            properties: {
              'local.server.port': {
                value: '8080'
              }
            }
          }
        ];
        spyOn(service, 'getBeans').and.returnValue(of(beans));
        spyOn(service, 'getPropertySources').and.returnValue(of(propertySources));

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(service.getBeans).toHaveBeenCalled();
        expect(service.getPropertySources).toHaveBeenCalled();
        expect(comp.allBeans).toEqual(beans);
        expect(comp.beans).toEqual(beans);
        expect(comp.propertySources).toEqual(propertySources);
      });
    });
  });
});
