# Generated by Django 2.2.1 on 2019-05-04 15:40

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('webportal', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='unit',
            name='short',
            field=models.CharField(default='', max_length=10),
        ),
    ]
