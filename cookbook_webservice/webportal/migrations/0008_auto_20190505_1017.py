# Generated by Django 2.2.1 on 2019-05-05 10:17

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('webportal', '0007_auto_20190505_1015'),
    ]

    operations = [
        migrations.AlterField(
            model_name='recipe',
            name='image',
            field=models.ImageField(blank=True, null=True, upload_to='images'),
        ),
    ]
